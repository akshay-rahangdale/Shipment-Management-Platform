package com.shipment.shipmentservice.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierResponse {

    private UUID id;
    private String name;
    private String code;
    private String contactEmail;
}