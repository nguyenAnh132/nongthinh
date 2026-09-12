package com.nongthinh.notification_service.infra.persistence.emailhistory;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Entity
@Table(name = "email_histories")
@AllArgsConstructor
@NoArgsConstructor
public class JpaEmailHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = true)
    private UUID userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "purpose_id", nullable = false)
    private Long purposeId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "send_at", nullable = false)
    private Instant sendAt;

}
