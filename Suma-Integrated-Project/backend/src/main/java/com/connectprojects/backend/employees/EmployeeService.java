package com.connectprojects.backend.employees;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.account.AppUserRepository;
import com.connectprojects.backend.account.UserRole;
import com.connectprojects.backend.api.ApiException;
import com.connectprojects.backend.attendance.AttendanceBreakResponse;
import com.connectprojects.backend.attendance.AttendanceRecord;
import com.connectprojects.backend.attendance.AttendanceRepository;
import com.connectprojects.backend.auth.CurrentUserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;
    private final AppUserRepository users;
    private final AttendanceRepository attendance;
    private final CurrentUserService currentUser;
    private final SalaryEncryptionService salaryEncryption;

    public EmployeeService(EmployeeRepository employees, AppUserRepository users, AttendanceRepository attendance,
                           CurrentUserService currentUser,
                           SalaryEncryptionService salaryEncryption) {
        this.employees = employees;
        this.users = users;
        this.attendance = attendance;
        this.currentUser = currentUser;
        this.salaryEncryption = salaryEncryption;
    }

    @Transactional(readOnly = true)
    public EmployeeDirectoryResponse list(Authentication authentication) {
        requireManager(authentication);
        List<EmployeeRecord> records = employees.findAllByOrderByFullNameAsc();
        return new EmployeeDirectoryResponse(
                records.size(),
                employees.countByStatus(EmployeeStatus.ACTIVE),
                records.stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeAccountOption> attendanceAccounts(Long employeeId, Authentication authentication) {
        requireManager(authentication);
        EmployeeRecord employee = employeeId == null ? null : find(employeeId);
        Set<Long> linkedUserIds = new HashSet<>(employees.findAllByLinkedUserIsNotNull().stream()
                .map(record -> record.getLinkedUser().getId())
                .toList());
        if (employee != null && employee.getLinkedUser() != null) {
            linkedUserIds.remove(employee.getLinkedUser().getId());
        }

        List<EmployeeAccountOption> options = users.findAllByEnabledTrueOrderByFullNameAsc().stream()
                .filter(user -> user.getRole() != UserRole.SUPER_USER && user.getRole() != UserRole.MANAGER)
                .filter(user -> user.getPermissions().contains("attendance.view"))
                .filter(user -> !linkedUserIds.contains(user.getId()))
                .map(EmployeeAccountOption::from)
                .collect(Collectors.toCollection(ArrayList::new));
        if (employee != null && employee.getLinkedUser() != null
                && options.stream().noneMatch(option -> option.id().equals(employee.getLinkedUser().getId()))) {
            options.add(EmployeeAccountOption.from(employee.getLinkedUser()));
        }
        return options;
    }

    @Transactional(readOnly = true)
    public List<EmployeeAttendanceResponse> attendance(LocalDate date, Authentication authentication) {
        requireManager(authentication);
        LocalDate workDate = date == null ? LocalDate.now() : date;
        List<EmployeeRecord> activeEmployees = employees.findAllByStatusOrderByFullNameAsc(EmployeeStatus.ACTIVE);
        List<Long> linkedUserIds = activeEmployees.stream()
                .map(EmployeeRecord::getLinkedUser)
                .filter(Objects::nonNull)
                .map(AppUser::getId)
                .toList();
        Map<Long, AttendanceRecord> recordsByUser = linkedUserIds.isEmpty()
                ? Map.of()
                : attendance.findAllByWorkDateAndUser_IdInOrderByUser_FullNameAsc(workDate, linkedUserIds)
                        .stream()
                        .collect(Collectors.toMap(record -> record.getUser().getId(), Function.identity()));

        return activeEmployees.stream().map(employee -> {
            AppUser linkedUser = employee.getLinkedUser();
            AttendanceRecord record = linkedUser == null ? null : recordsByUser.get(linkedUser.getId());
            List<AttendanceBreakResponse> breaks = record == null
                    ? List.of()
                    : record.getBreaks().stream().map(AttendanceBreakResponse::from).toList();
            return new EmployeeAttendanceResponse(
                    employee.getId(),
                    employee.getEmployeeId(),
                    employee.getFullName(),
                    linkedUser == null ? null : linkedUser.getUsername(),
                    workDate,
                    record == null ? null : record.getCheckIn(),
                    record == null ? null : record.getCheckOut(),
                    breaks);
        }).toList();
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request, Authentication authentication) {
        requireManager(authentication);
        String employeeId = normalizeEmployeeId(request.employeeId());
        if (employees.existsByEmployeeId(employeeId)) {
            throw duplicateEmployeeId();
        }
        AppUser linkedUser = findLinkableUser(request.linkedUserId(), null, null);
        EmployeeRecord record = new EmployeeRecord(
                employeeId,
                request.fullName().trim(),
                request.department().trim(),
                request.jobTitle().trim(),
                normalizeEmail(request.workEmail()),
                request.status(),
                salaryEncryption.encrypt(request.monthlySalary()),
                linkedUser);
        try {
            return toResponse(employees.saveAndFlush(record));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateEmployeeRecord();
        }
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request, Authentication authentication) {
        requireManager(authentication);
        EmployeeRecord record = find(id);
        String employeeId = normalizeEmployeeId(request.employeeId());
        if (employees.existsByEmployeeIdAndIdNot(employeeId, id)) {
            throw duplicateEmployeeId();
        }
        Long existingLinkedUserId = record.getLinkedUser() == null ? null : record.getLinkedUser().getId();
        AppUser linkedUser = findLinkableUser(request.linkedUserId(), id, existingLinkedUserId);
        record.update(
                employeeId,
                request.fullName().trim(),
                request.department().trim(),
                request.jobTitle().trim(),
                normalizeEmail(request.workEmail()),
                request.status(),
                salaryEncryption.encrypt(request.monthlySalary()),
                linkedUser);
        try {
            return toResponse(employees.saveAndFlush(record));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateEmployeeRecord();
        }
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        requireManager(authentication);
        employees.delete(find(id));
    }

    private void requireManager(Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        if (user.getRole() != UserRole.MANAGER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only Manager accounts can access the employee directory.");
        }
    }

    private EmployeeRecord find(Long id) {
        return employees.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found."));
    }

    private EmployeeResponse toResponse(EmployeeRecord record) {
        BigDecimal monthlySalary = salaryEncryption.decrypt(record.getMonthlySalaryEncrypted());
        BigDecimal annualSalary = monthlySalary.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP);
        AppUser linkedUser = record.getLinkedUser();
        return new EmployeeResponse(
                record.getId(),
                record.getEmployeeId(),
                record.getFullName(),
                record.getDepartment(),
                record.getJobTitle(),
                record.getWorkEmail(),
                record.getStatus(),
                monthlySalary,
                annualSalary,
                linkedUser == null ? null : linkedUser.getId(),
                linkedUser == null ? null : linkedUser.getFullName(),
                linkedUser == null ? null : linkedUser.getUsername());
    }

    private AppUser findLinkableUser(Long linkedUserId, Long employeeId, Long existingLinkedUserId) {
        if (linkedUserId == null) {
            return null;
        }
        boolean isCurrentLink = Objects.equals(linkedUserId, existingLinkedUserId);
        AppUser user = users.findById(linkedUserId)
                .filter(account -> isCurrentLink || account.isEnabled())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Active login account not found."));
        if (!isCurrentLink && (user.getRole() == UserRole.SUPER_USER
                || user.getRole() == UserRole.MANAGER
                || !user.getPermissions().contains("attendance.view"))) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Choose an active non-Manager employee account with attendance access.");
        }
        boolean alreadyLinked = !isCurrentLink && (employeeId == null
                ? employees.existsByLinkedUser_Id(linkedUserId)
                : employees.existsByLinkedUser_IdAndIdNot(linkedUserId, employeeId));
        if (alreadyLinked) {
            throw duplicateLinkedAccount();
        }
        return user;
    }

    private String normalizeEmployeeId(String employeeId) {
        return employeeId.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private ApiException duplicateEmployeeId() {
        return new ApiException(HttpStatus.CONFLICT, "That employee ID is already in use.");
    }

    private ApiException duplicateLinkedAccount() {
        return new ApiException(HttpStatus.CONFLICT, "That login account is already linked to another employee.");
    }

    private ApiException duplicateEmployeeRecord() {
        return new ApiException(HttpStatus.CONFLICT, "The employee ID or linked login account is already in use.");
    }
}
