package com.connectprojects.backend.attendance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AttendanceScheduleRequest(
        @Min(1) @Max(1440) int shiftMinutes,
        @Min(0) @Max(480) int breakMinutes) {
}
