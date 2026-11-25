package com.connectprojects.backend.employees;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private static final CacheControl NO_STORE = CacheControl.noStore();

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<EmployeeDirectoryResponse> list(Authentication authentication) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(service.list(authentication));
    }

    @GetMapping("/attendance-accounts")
    public ResponseEntity<List<EmployeeAccountOption>> attendanceAccounts(
            @RequestParam(required = false) Long employeeId,
            Authentication authentication) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(service.attendanceAccounts(employeeId, authentication));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<EmployeeAttendanceResponse>> attendance(
            @RequestParam(required = false) LocalDate date,
            Authentication authentication) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(service.attendance(date, authentication));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(NO_STORE)
                .body(service.create(request, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody EmployeeRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.ok()
                .cacheControl(NO_STORE)
                .body(service.update(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
        return ResponseEntity.noContent().cacheControl(NO_STORE).build();
    }
}
