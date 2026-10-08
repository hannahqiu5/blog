package com.hanqiu.blog.services;

import com.hanqiu.blog.services.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                authenticationService,
                "secretKey",
                "this-is-a-test-secret-key-that-is-long-enough"
        );
    }
    @Test
    void authenticate_shouldReturnUserDetails() {
        String email = "test@test.com";
        String password = "password123";

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);

        UserDetails result = authenticationService.authenticate(email, password);

        assertEquals(userDetails, result);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        verify(userDetailsService).loadUserByUsername(email);
    }

    @Test
    void authenticate_shouldThrowException() {
        String email = "test@test.com";
        String password = "wrongPassword";

        when(userDetailsService.loadUserByUsername(email))
                .thenThrow(new BadCredentialsException("Bad credentials"));

       assertThrows(BadCredentialsException.class,
               () -> authenticationService.authenticate(email, password));
    }

    @Test
    void generateToken_shouldReturnJwt() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername())
                .thenReturn("test@test.com");

        String result = authenticationService.generateToken(userDetails);

        assertNotNull(result);
    }

    @Test
    void validateToken_shouldReturnUserDetails() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername())
                .thenReturn("test@test.com");
        when(userDetailsService.loadUserByUsername("test@test.com"))
                .thenReturn(userDetails);

        String token = authenticationService.generateToken(userDetails);

        UserDetails result = authenticationService.validateToken(token);

        assertEquals(userDetails, result);

        verify(userDetailsService).loadUserByUsername("test@test.com");
    }

}
