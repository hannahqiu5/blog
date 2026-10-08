package com.hanqiu.blog.controller;

import com.hanqiu.blog.domain.dtos.AuthResponse;
import com.hanqiu.blog.domain.dtos.LoginRequest;
import com.hanqiu.blog.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@abc.com")
                .password("pass123")
                .build();
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationService.authenticate(request.getEmail(),
                request.getPassword()))
                .thenReturn(userDetails);
        when(authenticationService.generateToken(userDetails))
                .thenReturn("test-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@abc.com",
                                    "password": "pass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
        ;

        verify(authenticationService).authenticate(request.getEmail(), request.getPassword());
        verify(authenticationService).generateToken(userDetails);
    }


}
