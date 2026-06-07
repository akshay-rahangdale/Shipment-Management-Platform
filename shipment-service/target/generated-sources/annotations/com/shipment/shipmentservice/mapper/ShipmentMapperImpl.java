package com.shipment.shipmentservice.mapper;

import com.shipment.shipmentservice.dto.request.CreateShipmentRequest;
import com.shipment.shipmentservice.dto.request.UpdateShipmentRequest;
import com.shipment.shipmentservice.dto.response.CarrierResponse;
import com.shipment.shipmentservice.dto.response.CustomerResponse;
import com.shipment.shipmentservice.dto.response.ShipmentResponse;
import com.shipment.shipmentservice.dto.response.ShipmentSummaryResponse;
import com.shipment.shipmentservice.model.Carrier;
import com.shipment.shipmentservice.model.Customer;
import com.shipment.shipmentservice.model.Shipment;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T12:00:13+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260602-1458, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ShipmentMapperImpl implements ShipmentMapper {

    @Override
    public CustomerResponse toCustomerResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse.CustomerResponseBuilder customerResponse = CustomerResponse.builder();

        customerResponse.address( customer.getAddress() );
        customerResponse.email( customer.getEmail() );
        customerResponse.id( customer.getId() );
        customerResponse.name( customer.getName() );
        customerResponse.phone( customer.getPhone() );

        return customerResponse.build();
    }

    @Override
    public CarrierResponse toCarrierResponse(Carrier carrier) {
        if ( carrier == null ) {
            return null;
        }

        CarrierResponse.CarrierResponseBuilder carrierResponse = CarrierResponse.builder();

        carrierResponse.code( carrier.getCode() );
        carrierResponse.contactEmail( carrier.getContactEmail() );
        carrierResponse.id( carrier.getId() );
        carrierResponse.name( carrier.getName() );

        return carrierResponse.build();
    }

    @Override
    public ShipmentResponse toResponse(Shipment shipment) {
        if ( shipment == null ) {
            return null;
        }

        ShipmentResponse.ShipmentResponseBuilder shipmentResponse = ShipmentResponse.builder();

        shipmentResponse.actualDelivery( shipment.getActualDelivery() );
        shipmentResponse.carrier( toCarrierResponse( shipment.getCarrier() ) );
        shipmentResponse.createdAt( shipment.getCreatedAt() );
        shipmentResponse.declaredValue( shipment.getDeclaredValue() );
        shipmentResponse.destinationAddress( shipment.getDestinationAddress() );
        shipmentResponse.estimatedDelivery( shipment.getEstimatedDelivery() );
        shipmentResponse.id( shipment.getId() );
        shipmentResponse.originAddress( shipment.getOriginAddress() );
        shipmentResponse.recipient( toCustomerResponse( shipment.getRecipient() ) );
        shipmentResponse.sender( toCustomerResponse( shipment.getSender() ) );
        shipmentResponse.specialInstructions( shipment.getSpecialInstructions() );
        shipmentResponse.status( shipment.getStatus() );
        shipmentResponse.trackingNumber( shipment.getTrackingNumber() );
        shipmentResponse.updatedAt( shipment.getUpdatedAt() );
        shipmentResponse.weightKg( shipment.getWeightKg() );

        shipmentResponse.statusDescription( shipment.getStatus().getDescription() );
        shipmentResponse.slaAtRisk( shipment.isSlaAtRisk(3) );
        shipmentResponse.transitDays( shipment.getTransitDays() );

        return shipmentResponse.build();
    }

    @Override
    public ShipmentSummaryResponse toSummaryResponse(Shipment shipment) {
        if ( shipment == null ) {
            return null;
        }

        ShipmentSummaryResponse.ShipmentSummaryResponseBuilder shipmentSummaryResponse = ShipmentSummaryResponse.builder();

        shipmentSummaryResponse.createdAt( shipment.getCreatedAt() );
        shipmentSummaryResponse.destinationAddress( shipment.getDestinationAddress() );
        shipmentSummaryResponse.estimatedDelivery( shipment.getEstimatedDelivery() );
        shipmentSummaryResponse.id( shipment.getId() );
        shipmentSummaryResponse.status( shipment.getStatus() );
        shipmentSummaryResponse.trackingNumber( shipment.getTrackingNumber() );

        shipmentSummaryResponse.statusDescription( shipment.getStatus().getDescription() );
        shipmentSummaryResponse.slaAtRisk( shipment.isSlaAtRisk(3) );
        shipmentSummaryResponse.recipientName( shipment.getRecipient().getName() );

        return shipmentSummaryResponse.build();
    }

    @Override
    public Shipment toEntity(CreateShipmentRequest request) {
        if ( request == null ) {
            return null;
        }

        Shipment.ShipmentBuilder shipment = Shipment.builder();

        shipment.declaredValue( request.getDeclaredValue() );
        shipment.destinationAddress( request.getDestinationAddress() );
        shipment.estimatedDelivery( request.getEstimatedDelivery() );
        shipment.originAddress( request.getOriginAddress() );
        shipment.specialInstructions( request.getSpecialInstructions() );
        shipment.weightKg( request.getWeightKg() );

        return shipment.build();
    }

    @Override
    public void updateEntity(UpdateShipmentRequest request, Shipment shipment) {
        if ( request == null ) {
            return;
        }

        if ( request.getDestinationAddress() != null ) {
            shipment.setDestinationAddress( request.getDestinationAddress() );
        }
        if ( request.getEstimatedDelivery() != null ) {
            shipment.setEstimatedDelivery( request.getEstimatedDelivery() );
        }
        if ( request.getSpecialInstructions() != null ) {
            shipment.setSpecialInstructions( request.getSpecialInstructions() );
        }
        if ( request.getStatus() != null ) {
            shipment.setStatus( request.getStatus() );
        }
    }
}
