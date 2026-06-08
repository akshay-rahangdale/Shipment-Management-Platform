package com.shipment.trackingservice.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoLocation {

    private Double lat;
    private Double lng;
    private String city;
    private String countryCode;
    private String facilityCode;
}