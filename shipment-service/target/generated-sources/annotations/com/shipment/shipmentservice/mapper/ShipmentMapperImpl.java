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
    date = "2026-06-07T13:23:36+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class ShipmentMapperImpl implements ShipmentMapper {

    @Override
    public CustomerResponse toCustomerResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse.CustomerResponseBuilder customerResponse = CustomerResponse.builder();

        customerResponse.id( customer.getId() );
        customerResponse.name( customer.getName() );
        customerResponse.email( customer.getEmail() );
        customerResponse.phone( customer.getPhone() );
        customerResponse.address( customer.getAddress() );

        return customerResponse.build();
    }

    @Override
    public CarrierResponse toCarrierResponse(Carrier carrier) {
        if ( carrier == null ) {
            return null;
        }

        CarrierResponse.CarrierResponseBuilder carrierResponse = CarrierResponse.builder();

        carrierResponse.id( carrier.getId() );
        carrierResponse.name( carrier.getName() );
        carrierResponse.code( carrier.getCode() );
        carrierResponse.contactEmail( carrier.getContactEmail() );

        return carrierResponse.build();
    }

    @Override
    public ShipmentResponse toResponse(Shipment shipment) {
        if ( shipment == null ) {
            return null;
        }

        ShipmentResponse.ShipmentResponseBuilder shipmentResponse = ShipmentResponse.builder();

        shipmentResponse.id( shipment.getId() );
        shipmentResponse.trackingNumber( shipment.getTrackingNumber() );
        shipmentResponse.sender( toCustomerResponse( shipment.getSender() ) );
        shipmentResponse.recipient( toCustomerResponse( shipment.getRecipient() ) );
        shipmentResponse.carrier( toCarrierResponse( shipment.getCarrier() ) );
        shipmentResponse.status( shipment.getStatus() );
        shipmentResponse.originAddress( shipment.getOriginAddress() );
        shipmentResponse.destinationAddress( shipment.getDestinationAddress() );
        shipmentResponse.weightKg( shipment.getWeightKg() );
        shipmentResponse.declaredValue( shipment.getDeclaredValue() );
        shipmentResponse.estimatedDelivery( shipment.getEstimatedDelivery() );
        shipmentResponse.actualDelivery( shipment.getActualDelivery() );
        shipmentResponse.specialInstructions( shipment.getSpecialInstructions() );
        shipmentResponse.createdAt( shipment.getCreatedAt() );
        shipmentResponse.updatedAt( shipment.getUpdatedAt() );

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

        shipmentSummaryResponse.id( shipment.getId() );
        shipmentSummaryResponse.trackingNumber( shipment.getTrackingNumber() );
        shipmentSummaryResponse.status( shipment.getStatus() );
        shipmentSummaryResponse.destinationAddress( shipment.getDestinationAddress() );
        shipmentSummaryResponse.estimatedDelivery( shipment.getEstimatedDelivery() );
        shipmentSummaryResponse.createdAt( shipment.getCreatedAt() );

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

        shipment.originAddress( request.getOriginAddress() );
        shipment.destinationAddress( request.getDestinationAddress() );
        shipment.weightKg( request.getWeightKg() );
        shipment.declaredValue( request.getDeclaredValue() );
        shipment.estimatedDelivery( request.getEstimatedDelivery() );
        shipment.specialInstructions( request.getSpecialInstructions() );

        return shipment.build();
    }

    @Override
    public void updateEntity(UpdateShipmentRequest request, Shipment shipment) {
        if ( request == null ) {
            return;
        }

        if ( request.getStatus() != null ) {
            shipment.setStatus( request.getStatus() );
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
    }
}
