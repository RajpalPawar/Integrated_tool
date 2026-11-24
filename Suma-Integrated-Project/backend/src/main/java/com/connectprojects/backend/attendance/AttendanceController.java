package com.connectprojects.backend.attendance;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService service;

    public AttendanceController(AttendanceService service) { this.service = service; }

    @GetMapping("/today")
    public AttendanceResponse today(Authentication authentication) { return service.today(authentication); }

    @GetMapping("/my")
    public List<AttendanceResponse> mine(Authentication authentication) { return service.mine(authentication); }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse checkIn(Authentication authentication) { return service.checkIn(authentication); }

    @PostMapping("/check-out")
    public AttendanceResponse checkOut(Authentication authentication) { return service.checkOut(authentication); }

    @PostMapping("/break-start")
    public AttendanceResponse startBreak(@Valid @RequestBody AttendanceBreakRequest request,
                                         Authentication authentication) {
        return service.startBreak(request, authentication);
    }

    @PostMapping("/break-end")
    public AttendanceResponse endBreak(Authentication authentication) { return service.endBreak(authentication); }

    @GetMapping("/schedules")
    public List<AttendanceScheduleResponse> schedules(Authentication authentication) {
        return service.schedules(authentication);
    }

    @PutMapping("/schedules/{userId}")
    public AttendanceScheduleResponse updateSchedule(@PathVariable Long userId,
                                                     @Valid @RequestBody AttendanceScheduleRequest request,
                                                     Authentication authentication) {
        return service.updateSchedule(userId, request, authentication);
    }

    @GetMapping("/team")
    public List<AttendanceResponse> team(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        return service.team(date, authentication);
    }
}
