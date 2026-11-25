package com.connectprojects.backend.employees;

import com.connectprojects.backend.account.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "employee_directory")
public class EmployeeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, unique = true, length = 40)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(name = "job_title", nullable = false, length = 120)
    private String jobTitle;

    @Column(name = "work_email", length = 254)
    private String workEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "monthly_salary_encrypted", nullable = false, length = 512)
    private String monthlySalaryEncrypted;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id", unique = true)
    private AppUser linkedUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EmployeeRecord() {
    }

    public EmployeeRecord(String employeeId, String fullName, String department, String jobTitle,
                          String workEmail, EmployeeStatus status, String monthlySalaryEncrypted,
                          AppUser linkedUser) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.jobTitle = jobTitle;
        this.workEmail = workEmail;
        this.status = status;
        this.monthlySalaryEncrypted = monthlySalaryEncrypted;
        this.linkedUser = linkedUser;
    }

    public void update(String employeeId, String fullName, String department, String jobTitle,
                       String workEmail, EmployeeStatus status, String monthlySalaryEncrypted,
                       AppUser linkedUser) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.jobTitle = jobTitle;
        this.workEmail = workEmail;
        this.status = status;
        this.monthlySalaryEncrypted = monthlySalaryEncrypted;
        this.linkedUser = linkedUser;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public String getMonthlySalaryEncrypted() {
        return monthlySalaryEncrypted;
    }

    public AppUser getLinkedUser() {
        return linkedUser;
    }
}
