package com.connectprojects.backend.attendance;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.account.UserRole;
import com.connectprojects.backend.api.ApiException;
import com.connectprojects.backend.auth.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {
    private final AttendanceRepository records = mock(AttendanceRepository.class);
    private final AttendanceScheduleRepository schedules = mock(AttendanceScheduleRepository.class);
    private final com.connectprojects.backend.account.AppUserRepository users =
            mock(com.connectprojects.backend.account.AppUserRepository.class);
    private final CurrentUserService currentUser = mock(CurrentUserService.class);
    private final AttendanceService service = new AttendanceService(records, schedules, users, currentUser);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void managerCanViewTeamAttendance() {
        LocalDate date = LocalDate.now();
        when(currentUser.get(authentication)).thenReturn(user(UserRole.MANAGER));
        when(records.findAllByWorkDateOrderByUser_FullNameAsc(date)).thenReturn(List.of());

        assertEquals(List.of(), service.team(date, authentication));
        verify(records).findAllByWorkDateOrderByUser_FullNameAsc(date);
    }

    @Test
    void superUserCannotViewTeamAttendance() {
        when(currentUser.get(authentication)).thenReturn(user(UserRole.SUPER_USER));

        ApiException error = assertThrows(ApiException.class, () -> service.team(LocalDate.now(), authentication));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verifyNoInteractions(records);
    }

    @Test
    void nonManagerCannotViewSchedulesEvenWithAnAttendancePermission() {
        AppUser admin = user(UserRole.ADMIN);
        admin.setPermissions(List.of("attendance.manage"));
        when(currentUser.get(authentication)).thenReturn(admin);

        ApiException error = assertThrows(ApiException.class, () -> service.schedules(authentication));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verifyNoInteractions(users);
        verifyNoInteractions(schedules);
    }

    private AppUser user(UserRole role) {
        return new AppUser("test-user", "test@example.com", "Test User", "password-hash", role);
    }
}
