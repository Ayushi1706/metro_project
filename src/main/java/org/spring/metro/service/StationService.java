package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Station;
import org.spring.metro.repository.StationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public Page<Station> getAllStations(Pageable pageable) {
        return stationRepository.findAll(pageable);
    }

    public Station getStationById(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Station not found with id: " + id)
                );
    }

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public Station updateStation(String id, Station station) {

        Station existingStation = stationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Station not found with id: " + id)
                );

        existingStation.setStationCode(station.getStationCode());
        existingStation.setLatitude(station.getLatitude());
        existingStation.setLongitude(station.getLongitude());
        existingStation.setAddress(station.getAddress());
        existingStation.setOpenedDate(station.getOpenedDate());
        existingStation.setLineColor(station.getLineColor());
        existingStation.setIsActive(station.getIsActive());

        return stationRepository.save(existingStation);
    }

    public void deleteStation(String id) {

        Station station = stationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Station not found with id: " + id)
                );

        stationRepository.delete(station);
    }
}