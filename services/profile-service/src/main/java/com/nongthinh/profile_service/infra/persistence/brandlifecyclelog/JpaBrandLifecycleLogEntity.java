package com.nongthinh.profile_service.infra.persistence.brandlifecyclelog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "brand_lifecycle_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaBrandLifecycleLogEntity {

    @Id
    private UUID id;

    @Column(name = "brand_profile_id", nullable = false)
    private UUID brandProfileId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "from_status", length = 30)
    private String fromStatus;

    @Column(name = "to_status", length = 30)
    private String toStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
