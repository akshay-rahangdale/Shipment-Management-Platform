package com.shipment.trackingservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckpointUpdateRequest {

    @NotBlank
    private String status;

    @NotNull
    @PastOrPresent
    private LocalDateTime timestamp;

    @NotBlank
    private String scanSource;

    private Double lat;
    private Double lng;

    @NotBlank
    private String city;

    @NotBlank
    @Size(min = 2, max = 3)
    private String countryCode;

    private String facilityCode;
    private String exceptionCode;
    private String description;
}