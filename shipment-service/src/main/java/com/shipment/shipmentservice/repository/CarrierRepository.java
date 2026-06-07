package com.shipment.shipmentservice.repository;

import com.shipment.shipmentservice.model.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, UUID> {
    Optional<Carrier> findByCode(String code);

    Optional<Carrier>findByCodeIgnoreCase(String code);

    List<Carrier>findByActiveTrue();

    boolean existsByCode(String code);

    @Query("SELECT c FROM Carrier c LEFT JOIN FETCH c.slaPolicies WHERE c.id = :id")
    Optional<Carrier>findByIdWithSlaPolicies( UUID id);
    
}
      