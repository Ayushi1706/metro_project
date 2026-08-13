package org.spring.metro.repository;

import org.spring.metro.models.entity.MetroCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetroCardRepository extends JpaRepository<MetroCard, Long> {
}
