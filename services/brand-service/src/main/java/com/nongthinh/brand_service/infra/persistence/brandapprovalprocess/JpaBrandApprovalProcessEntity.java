package com.nongthinh.brand_service.infra.persistence.brandapprovalprocess;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "brand_approval_processes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaBrandApprovalProcessEntity {

    @Id
    private UUID id;

    @Column(name = "brand_profile_id", nullable = false, unique = true)
    private UUID brandProfileId;

    @Column(name = "camunda_process_instance_id", nullable = false, unique = true, length = 64)
    private String camundaProcessInstanceId;

    @Column(name = "camunda_business_key", nullable = false, unique = true, length = 128)
    private String camundaBusinessKey;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "assigned_reviewer_id")
    private UUID assignedReviewerId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
