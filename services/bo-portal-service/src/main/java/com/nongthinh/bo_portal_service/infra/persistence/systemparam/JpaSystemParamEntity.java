package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "system_params")
@Getter
@Setter
public class JpaSystemParamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "value", nullable = false, columnDefinition = "text")
    private String value;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(name = "system_defined", nullable = false)
    private boolean systemDefined;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
