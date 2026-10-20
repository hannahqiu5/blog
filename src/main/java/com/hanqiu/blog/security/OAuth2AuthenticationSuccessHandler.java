package com.hanqiu.blog.security;

import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.domain.enums.Role;
import com.hanqiu.blog.repositories.UserRepository;
import com.hanqiu.blog.services.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final UserDetailsService userDetailsService;

    @Value("${app.oauth2.success-redirect-url:http://localhost/oauth2/callback}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        String email = oauthToken.getPrincipal().getAttribute("email");

        if (email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Google account did not provide an email address"
            );
            return;
        }

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            String name = oauthToken.getPrincipal().getAttribute("name");

            if (name == null || name.isBlank()) {
                name = email.substring(0, email.indexOf('@'));
            }

            User newUser = User.builder()
                    .email(email)
                    .password(null)
                    .name(name)
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(newUser);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwt = authenticationService.generateToken(userDetails);

        response.sendRedirect(successRedirectUrl + "#token=" + jwt);
    }
}
