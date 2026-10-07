package org.spring.metro.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.spring.metro.models.dto.FareDto;
import org.spring.metro.models.entity.Fare;
import org.spring.metro.models.entity.Station;
import org.spring.metro.repository.FareRepository;
import org.spring.metro.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FareService {

    private final FareRepository fareRepository;
    private final StationRepository stationRepository;

    @Transactional
    public FareDto createFare(FareDto dto) {
        if (dto.fareId() == null || dto.fareId().isBlank()) {
            throw new IllegalArgumentException("fareId is required");
        }
        if (fareRepository.existsById(dto.fareId())) {
            throw new IllegalStateException("Fare already exists: " + dto.fareId());
        }

        Station source = findStation(dto.sourceStationId());
        Station dest = findStation(dto.destinationStationId());
        validate(source, dest, dto.baseFare());

        if (fareRepository.existsBySourceStationAndDestinationStation(source, dest)) {
            throw new IllegalStateException("A fare already exists for this station pair");
        }

        Fare fare = Fare.builder()
                .fareId(dto.fareId())
                .sourceStation(source)
                .destinationStation(dest)
                .baseFare(dto.baseFare().doubleValue())
                .build();

        return toDto(fareRepository.save(fare));
    }

    @Transactional(readOnly = true)
    public FareDto getFare(String fareId) {
        return toDto(findFare(fareId));
    }

    @Transactional(readOnly = true)
    public List<FareDto> getAllFares() {
        return fareRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public FareDto getFareBetween(String sourceStationId, String destinationStationId) {
        Station source = findStation(sourceStationId);
        Station dest = findStation(destinationStationId);
        return fareRepository.findBySourceStationAndDestinationStation(source, dest)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No fare defined for " + sourceStationId + " -> " + destinationStationId));
    }

    @Transactional
    public FareDto updateFare(String fareId, FareDto dto) {
        Fare fare = findFare(fareId);

        Station source = findStation(dto.sourceStationId());
        Station dest = findStation(dto.destinationStationId());
        validate(source, dest, dto.baseFare());

        fareRepository.findBySourceStationAndDestinationStation(source, dest)
                .filter(existing -> !existing.getFareId().equals(fareId))
                .ifPresent(existing -> {
                    throw new IllegalStateException("A fare already exists for this station pair");
                });

        fare.setSourceStation(source);
        fare.setDestinationStation(dest);
        fare.setBaseFare(dto.baseFare().doubleValue());

        return toDto(fareRepository.save(fare));
    }

    @Transactional
    public void deleteFare(String fareId) {
        fareRepository.delete(findFare(fareId));
    }

    private void validate(Station source, Station dest, BigDecimal baseFare) {
        if (source.getStationId().equals(dest.getStationId())) {
            throw new IllegalArgumentException("Source and destination stations must be different");
        }
        if (baseFare == null || baseFare.signum() < 0) {
            throw new IllegalArgumentException("baseFare must be zero or positive");
        }
    }

    private Fare findFare(String fareId) {
        return fareRepository.findById(fareId)
                .orElseThrow(() -> new EntityNotFoundException("Fare not found: " + fareId));
    }

    private Station findStation(String stationId) {
        if (stationId == null) {
            throw new IllegalArgumentException("Station id is required");
        }
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));
    }

    private FareDto toDto(Fare f) {
        return new FareDto(
                f.getFareId(),
                f.getSourceStation().getStationId(),
                f.getDestinationStation().getStationId(),
                BigDecimal.valueOf(f.getBaseFare())
        );
    }
}