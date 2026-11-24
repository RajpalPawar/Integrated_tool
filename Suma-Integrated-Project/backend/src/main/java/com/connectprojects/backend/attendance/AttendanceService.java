package com.connectprojects.backend.attendance;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.account.AppUserRepository;
import com.connectprojects.backend.auth.CurrentUserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceRepository records;
    private final AttendanceScheduleRepository schedules;
    private final AppUserRepository users;
    private final CurrentUserService currentUser;

    public AttendanceService(AttendanceRepository records, AttendanceScheduleRepository schedules,
                            AppUserRepository users, CurrentUserService currentUser) {
        this.records = records;
        this.schedules = schedules;
        this.users = users;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public AttendanceResponse today(Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        return records.findByUser_IdAndWorkDate(user.getId(), LocalDate.now())
                .map(AttendanceResponse::from).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> mine(Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        return records.findAllByUser_IdOrderByWorkDateDesc(user.getId()).stream()
                .map(AttendanceResponse::from).toList();
    }

    @Transactional
    public AttendanceResponse checkIn(Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        LocalDate today = LocalDate.now();
        if (records.findByUser_IdAndWorkDate(user.getId(), today).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already checked in today.");
        }
        try {
            return AttendanceResponse.from(records.saveAndFlush(new AttendanceRecord(user, today, LocalTime.now().withNano(0))));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Today's attendance has already been recorded.");
        }
    }

    @Transactional
    public AttendanceResponse checkOut(Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        AttendanceRecord record = records.findByUser_IdAndWorkDate(user.getId(), LocalDate.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check in before checking out."));
        try {
            record.checkOut(LocalTime.now().withNano(0));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return AttendanceResponse.from(record);
    }

    @Transactional
    public AttendanceResponse startBreak(AttendanceBreakRequest request, Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        AttendanceRecord record = records.findByUser_IdAndWorkDate(user.getId(), LocalDate.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check in before starting a break."));
        try {
            record.startBreak(request.normalizedBreakType(), LocalTime.now().withNano(0));
            records.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A break is already in progress.");
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return AttendanceResponse.from(record);
    }

    @Transactional
    public AttendanceResponse endBreak(Authentication authentication) {
        AppUser user = currentUser.get(authentication);
        AttendanceRecord record = records.findByUser_IdAndWorkDate(user.getId(), LocalDate.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check in before ending a break."));
        try {
            record.endBreak(LocalTime.now().withNano(0));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
        return AttendanceResponse.from(record);
    }

    @Transactional(readOnly = true)
    public List<AttendanceScheduleResponse> schedules(Authentication authentication) {
        currentUser.require(authentication, "attendance.manage");
        return users.findAllByEnabledTrueOrderByFullNameAsc().stream()
                .map(user -> schedules.findByUser_Id(user.getId())
                        .map(AttendanceScheduleResponse::from)
                        .orElseGet(() -> new AttendanceScheduleResponse(user.getId(), user.getFullName(),
                                user.getUsername(), 540, 60)))
                .toList();
    }

    @Transactional
    public AttendanceScheduleResponse updateSchedule(Long userId, AttendanceScheduleRequest request,
                                                    Authentication authentication) {
        currentUser.require(authentication, "attendance.manage");
        AppUser user = users.findById(userId)
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active user not found."));
        AttendanceSchedule schedule = schedules.findById(userId).orElseGet(
                () -> new AttendanceSchedule(user, request.shiftMinutes(), request.breakMinutes()));
        schedule.update(request.shiftMinutes(), request.breakMinutes());
        return AttendanceScheduleResponse.from(schedules.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> team(LocalDate date, Authentication authentication) {
        currentUser.require(authentication, "attendance.manage");
        LocalDate requestedDate = date == null ? LocalDate.now() : date;
        return records.findAllByWorkDateOrderByUser_FullNameAsc(requestedDate).stream()
                .map(AttendanceResponse::from).toList();
    }
}
