package com.retailpos.role.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permission")
public class Permission extends BaseAuditableEntity {

    @Column(name = "code", nullable = false, length = 80, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "description", length = 500)
    private String description;
}
