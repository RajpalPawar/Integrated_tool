package com.connectprojects.backend.employees;

import java.math.BigDecimal;

public record EmployeeResponse(
        Long id,
        String employeeId,
        String fullName,
        String department,
        String jobTitle,
        String workEmail,
        EmployeeStatus status,
        BigDecimal monthlySalary,
        BigDecimal annualSalary,
        Long linkedUserId,
        String linkedUserName,
        String linkedUsername
) {
}
