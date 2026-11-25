package com.connectprojects.backend.employees;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.account.AppUserRepository;
import com.connectprojects.backend.account.UserRole;
import com.connectprojects.backend.api.ApiException;
import com.connectprojects.backend.attendance.AttendanceRecord;
import com.connectprojects.backend.attendance.AttendanceRepository;
import com.connectprojects.backend.auth.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmployeeServiceTest {
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final AttendanceRepository attendance = mock(AttendanceRepository.class);
    private final CurrentUserService currentUser = mock(CurrentUserService.class);
    private final SalaryEncryptionService encryption = mock(SalaryEncryptionService.class);
    private final EmployeeService service = new EmployeeService(employees, users, attendance, currentUser, encryption);
    private final Authentication authentication = mock(Authentication.class);

    @Test
    void managerCanViewDirectory() {
        when(currentUser.get(authentication)).thenReturn(user(UserRole.MANAGER));
        when(employees.findAllByOrderByFullNameAsc()).thenReturn(List.of());
        when(employees.countByStatus(EmployeeStatus.ACTIVE)).thenReturn(0L);

        EmployeeDirectoryResponse response = service.list(authentication);

        assertEquals(0, response.employeeCount());
        verify(employees).findAllByOrderByFullNameAsc();
    }

    @Test
    void superUserIsDeniedDespiteTheGlobalSuperUserPermissionBypass() {
        when(currentUser.get(authentication)).thenReturn(user(UserRole.SUPER_USER));

        ApiException exception = assertThrows(ApiException.class, () -> service.list(authentication));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(employees);
        verifyNoInteractions(encryption);
    }

    @Test
    void nonManagerCannotViewEmployeeAttendance() {
        when(currentUser.get(authentication)).thenReturn(user(UserRole.SUPER_USER));

        ApiException exception = assertThrows(ApiException.class, () ->
                service.attendance(LocalDate.now(), authentication));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(employees);
        verifyNoInteractions(attendance);
    }

    @Test
    void managerCanViewLinkedEmployeeAttendanceAndBreakIntervals() {
        LocalDate workDate = LocalDate.now();
        AppUser employeeLogin = mock(AppUser.class);
        when(employeeLogin.getId()).thenReturn(41L);
        EmployeeRecord employee = new EmployeeRecord(
                "EMP-1", "Roster Name", "Operations", "Analyst", null,
                EmployeeStatus.ACTIVE, "encrypted", employeeLogin);
        AttendanceRecord record = new AttendanceRecord(employeeLogin, workDate, LocalTime.of(9, 0));
        record.startBreak("GENERAL", LocalTime.of(12, 0));
        record.endBreak(LocalTime.of(12, 20));
        when(currentUser.get(authentication)).thenReturn(user(UserRole.MANAGER));
        when(employees.findAllByStatusOrderByFullNameAsc(EmployeeStatus.ACTIVE)).thenReturn(List.of(employee));
        when(attendance.findAllByWorkDateAndUser_IdInOrderByUser_FullNameAsc(workDate, List.of(41L)))
                .thenReturn(List.of(record));

        List<EmployeeAttendanceResponse> rows = service.attendance(workDate, authentication);

        assertEquals(1, rows.size());
        assertEquals("Roster Name", rows.get(0).employeeName());
        assertEquals(LocalTime.of(9, 0), rows.get(0).checkIn());
        assertEquals(20, rows.get(0).breaks().get(0).durationMinutes());
        verify(attendance).findAllByWorkDateAndUser_IdInOrderByUser_FullNameAsc(workDate, List.of(41L));
    }

    private AppUser user(UserRole role) {
        AppUser user = new AppUser("test-user", "test@example.com", "Test User", "password-hash", role);
        user.setPermissions(com.connectprojects.backend.account.PermissionCatalog.defaults(role));
        return user;
    }
}
