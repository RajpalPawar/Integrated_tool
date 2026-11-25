package com.connectprojects.backend.employees;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeRecord, Long> {
    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmployeeIdAndIdNot(String employeeId, Long id);

    boolean existsByLinkedUser_Id(Long linkedUserId);

    boolean existsByLinkedUser_IdAndIdNot(Long linkedUserId, Long id);

    List<EmployeeRecord> findAllByOrderByFullNameAsc();

    List<EmployeeRecord> findAllByLinkedUserIsNotNull();

    List<EmployeeRecord> findAllByStatusOrderByFullNameAsc(EmployeeStatus status);

    long countByStatus(EmployeeStatus status);
}
