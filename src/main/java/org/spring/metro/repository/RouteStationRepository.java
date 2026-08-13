package org.spring.metro.repository;

import org.spring.metro.models.entity.RouteStation;
import org.spring.metro.models.entity.RouteStationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteStationRepository extends JpaRepository<RouteStation, RouteStationId> {
}