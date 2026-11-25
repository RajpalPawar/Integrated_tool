package com.connectprojects.backend.employees;

import java.util.List;

public record EmployeeDirectoryResponse(
        long employeeCount,
        long activeEmployeeCount,
        List<EmployeeResponse> employees
) {
}
