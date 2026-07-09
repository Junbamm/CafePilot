package com.cafepilot.domain.cafe.entity;

import com.cafepilot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cafes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean isOpen;

    @Column
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    @Builder
    private Cafe(Long ownerId, String name, String address, String phone) {
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.isOpen = false;
    }

    public static Cafe create(Long ownerId, String name, String address, String phone) {
        return Cafe.builder()
                .ownerId(ownerId)
                .name(name)
                .address(address)
                .phone(phone)
                .build();
    }

    public void update(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void open() {
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.ownerId.equals(memberId);
    }
}
