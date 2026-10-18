package com.hanqiu.blog.security;

import com.hanqiu.blog.domain.entities.User;
import com.hanqiu.blog.repositories.UserRepository;
import com.hanqiu.blog.services.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        String email = oauthToken.getPrincipal().getAttribute("email");

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            User newUser = User.builder()
                    .email(email)
                    .password(null)
                    .name(oauthToken.getPrincipal().getName())
                    .createdAt(LocalDateTime.now()).build();
            userRepository.save(newUser);

        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwt = authenticationService.generateToken(userDetails);

        response.setContentType("application/json");
        response.getWriter().write(jwt);
    }
}
