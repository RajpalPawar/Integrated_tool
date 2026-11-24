package com.connectprojects.backend.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceScheduleRepository extends JpaRepository<AttendanceSchedule, Long> {
    Optional<AttendanceSchedule> findByUser_Id(Long userId);
    List<AttendanceSchedule> findAllByUser_EnabledTrueOrderByUser_FullNameAsc();
}
