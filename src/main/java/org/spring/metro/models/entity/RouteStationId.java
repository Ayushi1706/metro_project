package org.spring.metro.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RouteStationId implements Serializable {

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "station_id")
    private String stationId;
}