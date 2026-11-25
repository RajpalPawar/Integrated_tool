package com.connectprojects.backend.employees;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EmployeeRequest(
        @NotBlank @Size(max = 40) String employeeId,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 120) String department,
        @NotBlank @Size(max = 120) String jobTitle,
        @Email @Size(max = 254) String workEmail,
        @NotNull EmployeeStatus status,
        @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal monthlySalary,
        Long linkedUserId
) {
}
