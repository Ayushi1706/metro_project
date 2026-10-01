package org.spring.metro.service;

import lombok.RequiredArgsConstructor;
import org.spring.metro.models.entity.Train;
import org.spring.metro.repository.TrainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;

    public Page<Train> getAllTrains(Pageable pageable) {
        return trainRepository.findAll(pageable);
    }

    public Train getTrainById(String id) {
        return trainRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Train not found with id: " + id)
                );
    }

    public Train createTrain(Train train) {
        return trainRepository.save(train);
    }

    public Train updateTrain(String id, Train train) {

        Train existingTrain = trainRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Train not found with id: " + id)
                );

        existingTrain.setTrainNumber(train.getTrainNumber());
        existingTrain.setCapacity(train.getCapacity());
        existingTrain.setTotalCoaches(train.getTotalCoaches());
        existingTrain.setManufactureYear(train.getManufactureYear());
        existingTrain.setCreatedAt(train.getCreatedAt());
        existingTrain.setIsActive(train.getIsActive());

        return trainRepository.save(existingTrain);
    }

    public void deleteTrain(String id) {

        Train train = trainRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Train not found with id: " + id)
                );

        trainRepository.delete(train);
    }
}