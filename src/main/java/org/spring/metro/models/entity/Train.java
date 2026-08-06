package org.spring.metro.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "train")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Train {

    @Id
    @Column(name = "train_id")
    private String trainId;

    @Column(name = "train_number", nullable = false, unique = true)
    private String trainNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "total_coaches", nullable = false)
    private Integer totalCoaches;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}