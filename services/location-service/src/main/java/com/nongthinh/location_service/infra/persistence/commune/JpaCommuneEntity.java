package com.nongthinh.location_service.infra.persistence.commune;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "communes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaCommuneEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "province_id", length = 10, nullable = false)
    private String provinceId;

    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}
