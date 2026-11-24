package com.connectprojects.backend.auth;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.account.AppUserRepository;
import com.connectprojects.backend.account.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class AuthServiceTest {
    @Test
    void acceptsEmailAndAuthenticatesWithAccountUsername() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AppUserRepository users = mock(AppUserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        AppUser account = new AppUser(
                "sample.user", "sample.user@example.com", "Sample User", "password-hash", UserRole.SUPER_USER);
        when(users.findByUsername("sample.user@example.com")).thenReturn(Optional.empty());
        when(users.findByEmail("sample.user@example.com")).thenReturn(Optional.of(account));
        when(jwtService.issueToken("sample.user")).thenReturn("issued-token");

        AuthService service = new AuthService(authenticationManager, users, jwtService,
                mock(PasswordEncoder.class), mock(EmailService.class), "http://localhost");

        LoginResponse response = service.login(new LoginRequest(" Sample.User@Example.com ", "correct-password"));

        ArgumentCaptor<Authentication> authentication = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(authentication.capture());
        assertEquals("sample.user", authentication.getValue().getName());
        assertEquals("issued-token", response.token());
        assertEquals("sample.user", response.user().username());
    }

    @Test
    void continuesToAcceptUsernameSignIn() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AppUserRepository users = mock(AppUserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        AppUser account = new AppUser(
                "sample.user", "sample.user@example.com", "Sample User", "password-hash", UserRole.SUPER_USER);
        when(users.findByUsername("sample.user")).thenReturn(Optional.of(account));
        when(jwtService.issueToken("sample.user")).thenReturn("issued-token");

        AuthService service = new AuthService(authenticationManager, users, jwtService,
                mock(PasswordEncoder.class), mock(EmailService.class), "http://localhost");

        LoginResponse response = service.login(new LoginRequest("SAMPLE.USER", "correct-password"));

        verify(authenticationManager).authenticate(any(Authentication.class));
        assertEquals("sample.user", response.user().username());
    }
}
