package com.connectprojects.backend.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByUser_IdAndWorkDate(Long userId, LocalDate workDate);
    List<AttendanceRecord> findAllByWorkDateOrderByUser_FullNameAsc(LocalDate workDate);
    List<AttendanceRecord> findAllByWorkDateAndUser_IdInOrderByUser_FullNameAsc(LocalDate workDate, List<Long> userIds);
    List<AttendanceRecord> findAllByUser_IdOrderByWorkDateDesc(Long userId);
}
