package com.yaxinaz.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean deleted = false;

    private Instant deletedAt;

    private Long deletedBy;

    public void markDeleted(Long deletedByUserId) {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedByUserId;
    }
}
