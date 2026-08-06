package org.spring.metro.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "station")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {

    @Id
    @Column(name = "station_id")
    private String stationId;

    @Column(name = "station_code", nullable = false, unique = true)
    private String stationCode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String address;

    @Column(name = "opened_date")
    private LocalDate openedDate;

    @Column(name = "line_color", nullable = false)
    private String lineColor;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}