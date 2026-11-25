package com.connectprojects.backend.account;

import com.connectprojects.backend.api.ApiException;
import com.connectprojects.backend.auth.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final UserService service = new UserService(
            users,
            mock(PasswordEncoder.class),
            mock(CurrentUserService.class)
    );

    @Test
    void superUserCannotEditManagerAccount() {
        AppUser manager = user(UserRole.MANAGER);
        when(users.findById(7L)).thenReturn(Optional.of(manager));

        ApiException error = assertThrows(ApiException.class, () ->
                service.update(7L, new UserUpdateRequest("Changed", "changed@example.com", "Admin")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        assertEquals(UserRole.MANAGER, manager.getRole());
        verify(users).findById(7L);
        verify(users, org.mockito.Mockito.never()).save(manager);
    }

    @Test
    void superUserCannotChangeManagerPermissions() {
        when(users.findById(7L)).thenReturn(Optional.of(user(UserRole.MANAGER)));

        ApiException error = assertThrows(ApiException.class, () ->
                service.updatePermissions(7L, java.util.List.of("users.delete")));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void superUserCannotDeleteManagerAccount() {
        AppUser manager = user(7L, UserRole.MANAGER);
        when(users.findById(7L)).thenReturn(Optional.of(manager));

        ApiException error = assertThrows(ApiException.class, () ->
                service.delete(7L, user(1L, UserRole.SUPER_USER)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verify(users, org.mockito.Mockito.never()).delete(manager);
    }

    @Test
    void managerCanDeleteOnlyTheirOwnAccount() {
        AppUser ownAccount = user(7L, UserRole.MANAGER);
        when(users.findById(7L)).thenReturn(Optional.of(ownAccount));

        service.delete(7L, ownAccount);

        verify(users).delete(ownAccount);
    }

    @Test
    void managerCannotDeleteAnotherManagerAccount() {
        AppUser target = user(7L, UserRole.MANAGER);
        when(users.findById(7L)).thenReturn(Optional.of(target));

        ApiException error = assertThrows(ApiException.class, () ->
                service.delete(7L, user(8L, UserRole.MANAGER)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verify(users, org.mockito.Mockito.never()).delete(target);
    }

    @Test
    void superUserCanEditAndDeleteOtherRoles() {
        AppUser account = user(UserRole.TEAM_MEMBER);
        when(users.findById(7L)).thenReturn(Optional.of(account));
        when(users.existsByEmailAndIdNot("updated@example.com", 7L)).thenReturn(false);

        service.update(7L, new UserUpdateRequest("Updated Name", " UPDATED@example.com ", "Admin"));

        assertEquals("Updated Name", account.getFullName());
        assertEquals("updated@example.com", account.getEmail());
        assertEquals(UserRole.ADMIN, account.getRole());
        verify(users).findById(7L);

        when(users.findById(7L)).thenReturn(Optional.of(account));
        service.delete(7L, user(1L, UserRole.SUPER_USER));
        verify(users).delete(account);
    }

    @Test
    void editingAccountFieldsKeepsCustomPermissionsWhenRoleIsUnchanged() {
        AppUser account = user(UserRole.TEAM_MEMBER);
        account.setPermissions(List.of("users.view"));
        when(users.findById(7L)).thenReturn(Optional.of(account));
        when(users.existsByEmailAndIdNot("updated@example.com", 7L)).thenReturn(false);

        service.update(7L, new UserUpdateRequest("Updated Name", "updated@example.com", "Team Member"));

        assertEquals(List.of("users.view"), account.getPermissions());
    }

    @Test
    void nonSuperUserCannotDeleteOtherAccounts() {
        AppUser target = user(7L, UserRole.TEAM_MEMBER);
        when(users.findById(7L)).thenReturn(Optional.of(target));

        ApiException error = assertThrows(ApiException.class, () ->
                service.delete(7L, user(8L, UserRole.ADMIN)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verify(users, org.mockito.Mockito.never()).delete(target);
    }

    private AppUser user(UserRole role) {
        return new AppUser("user", "user@example.com", "User", "password-hash", role);
    }

    private AppUser user(Long id, UserRole role) {
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(id);
        when(user.getRole()).thenReturn(role);
        return user;
    }
}
