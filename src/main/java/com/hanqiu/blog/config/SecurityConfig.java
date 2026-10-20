package com.hanqiu.blog.config;

import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.domain.enums.Role;
import com.hanqiu.blog.repositories.UserRepository;
import com.hanqiu.blog.security.BlogUserDetailsService;
import com.hanqiu.blog.security.JwtAuthenticationFilter;
import com.hanqiu.blog.security.OAuth2AuthenticationSuccessHandler;
import com.hanqiu.blog.services.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private static final Set<String> SAFE_PROVIDER_ERROR_CODES = Set.of(
            "invalid_client", "invalid_grant", "unauthorized_client", "invalid_request",
            "unsupported_grant_type", "invalid_scope", "access_denied"
    );
    private static final Pattern PROVIDER_ERROR_CODE_PATTERN = Pattern.compile(
            "(?i)\\b(invalid_client|invalid_grant|unauthorized_client|invalid_request|unsupported_grant_type|invalid_scope|access_denied)\\b"
    );

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authenticationService) {
        return new JwtAuthenticationFilter(authenticationService);
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        BlogUserDetailsService blogUserDetailsService = new BlogUserDetailsService(userRepository);

        userRepository.findByEmail("test@abc.com").orElseGet(() -> {
            User newUser = User.builder()
                    .name("Test User")
                    .email("test@abc.com")
                    .password(passwordEncoder().encode("pass123!"))
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        });


        return blogUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler
    ) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/drafts").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()

                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String errorCode = exception instanceof OAuth2AuthenticationException oauthException
                                    ? oauthException.getError().getErrorCode()
                                    : exception.getClass().getSimpleName();
                            String providerErrorCode = findProviderErrorCode(exception);
                            String causeType = exception.getCause() == null
                                    ? "none"
                                    : exception.getCause().getClass().getSimpleName();
                            logger.warn("OAuth2 login failed; errorCode={}, providerErrorCode={}, causeType={}",
                                    errorCode, providerErrorCode, causeType);
                            response.sendRedirect("/login?error");
                        }));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static String findProviderErrorCode(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof OAuth2AuthenticationException oauthException) {
                String code = oauthException.getError().getErrorCode();
                if (SAFE_PROVIDER_ERROR_CODES.contains(code)) {
                    return code;
                }
            }
            String message = cause.getMessage();
            if (message != null) {
                Matcher matcher = PROVIDER_ERROR_CODE_PATTERN.matcher(message);
                if (matcher.find()) {
                    return matcher.group(1).toLowerCase(Locale.ROOT);
                }
            }
        }
        return "unavailable";
    }

}
