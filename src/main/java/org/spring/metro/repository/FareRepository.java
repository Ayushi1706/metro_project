package org.spring.metro.repository;

import org.spring.metro.models.entity.Fare;
import org.spring.metro.models.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRepository extends JpaRepository<Fare, String> {

    Optional<Fare> findBySourceStationAndDestinationStation(Station source, Station dest);

    boolean existsBySourceStationAndDestinationStation(Station source, Station dest);
}