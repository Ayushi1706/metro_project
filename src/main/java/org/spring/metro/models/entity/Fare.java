package org.spring.metro.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fare")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fare {

    @Id
    @Column(name = "fare_id")
    private String fareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_station_id", nullable = false)
    private Station sourceStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_station_id", nullable = false)
    private Station destinationStation;

    @Column(name = "base_fare", nullable = false)
    private Double baseFare;
}