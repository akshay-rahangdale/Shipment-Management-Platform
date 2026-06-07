package com.shipment.shipmentservice.repository;

import com.shipment.shipmentservice.model.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

    List<SlaPolicy> findByCarrierId(UUID carrierId);

    @Query("""
        SELECT s FROM SlaPolicy s
        WHERE s.carrier.id = :carrierId
          AND s.serviceTier = :serviceTier
          AND s.effectiveFrom <= :now
          AND (s.effectiveTo IS NULL OR s.effectiveTo > :now)
        """)
    Optional<SlaPolicy> findActivePolicy(UUID carrierId, String serviceTier, LocalDateTime now);

    @Query("""
        SELECT s FROM SlaPolicy s
        WHERE s.effectiveFrom <= :now
          AND (s.effectiveTo IS NULL OR s.effectiveTo > :now)
        """)
    List<SlaPolicy> findAllActivePolicies(LocalDateTime now);
}