package com.nongthinh.bo_portal_service.infra.persistence.fileconfig;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file_types")
@Getter
@Setter
public class JpaFileTypeEntity {
    @Id
    @Column(length = 20)
    private String code;

    @Column(name = "content_type", nullable = false, unique = true, length = 100)
    private String contentType;

    @Column(nullable = false, length = 10)
    private String extension;
}
