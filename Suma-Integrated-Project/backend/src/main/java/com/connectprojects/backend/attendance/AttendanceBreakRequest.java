package com.connectprojects.backend.attendance;

import jakarta.validation.constraints.Pattern;

public record AttendanceBreakRequest(
        @Pattern(regexp = "GENERAL|MEAL|REST") String breakType) {
    public String normalizedBreakType() {
        return breakType == null || breakType.isBlank() ? "GENERAL" : breakType;
    }
}
