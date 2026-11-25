package com.connectprojects.backend.employees;

import com.connectprojects.backend.account.AppUser;

public record EmployeeAccountOption(Long id, String username, String fullName, String role) {
    static EmployeeAccountOption from(AppUser user) {
        return new EmployeeAccountOption(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name());
    }
}
