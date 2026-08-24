package com.samir.vortex.bhs.checkin.repository;

import com.samir.vortex.bhs.checkin.model.Bag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BagRepository extends JpaRepository<Bag, UUID> {
    Optional<Bag> findByBagTag(String bagTag);
    boolean existsByBagTag(String bagTag);
}