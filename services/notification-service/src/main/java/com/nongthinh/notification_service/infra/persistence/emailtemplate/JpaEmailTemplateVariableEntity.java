package com.nongthinh.notification_service.infra.persistence.emailtemplate;

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
@Table(name = "email_template_variables")
@Getter
@Setter
public class JpaEmailTemplateVariableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purpose_id", nullable = false)
    private Long purposeId;

    @Column(name = "variable_name", nullable = false)
    private String variableName;

    @Column(name = "description")
    private String description;

    @Column(name = "example_value")
    private String exampleValue;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
