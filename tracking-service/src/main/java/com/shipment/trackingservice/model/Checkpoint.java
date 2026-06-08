package com.shipment.trackingservice.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Checkpoint {

    private Integer seq;
    private String status;
    private GeoLocation location;
    private LocalDateTime timestamp;
    private String scanSource;
    private String exceptionCode;
    private String description;
}