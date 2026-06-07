package com.shipment.shipmentservice.model.enums;

public enum EventType{
    SHIPMENT_CREATED("Shipment record created in the system", true),
            PICKED_UP("Package collected from sender", true),
            DEPARTED_FACILITY("Shipment left a warehouse or hub", true),
            ARRIVED_FACILITY("Shipment arrived at a warehouse or hub", true),
            OUT_FOR_DELIVERY("Loaded onto delivery vehicle", true),
            DELIVERED("Successfully delivered to recipient", true),
            DELIVERY_ATTEMPTED("Delivery attempted, recipient not available", true),
            RETURNED_TO_SENDER("Shipment returned to origin", true),
            CANCELLED("Shipment cancelled", true),
            CHECKPOINT_SCAN("Package scanned at a checkpoint", false),
            GPS_UPDATE("Automatic GPS location update from vehicle", false),
            WEATHER_DELAY("Delayed due to weather conditions", false),
            ADDRESS_ISSUE("Problem with delivery address", false),
            CUSTOMS_HOLD("Held at customs inspection", false),
            DAMAGED("Package reported as damaged", false),
            LOST("Package reported as lost", false),
            SECURITY_HOLD("Held for security screening", false),
            REROUTED("Shipment rerouted to different facility", false),
            PRIORITY_UPGRADED("Service level upgraded", false),
            CUSTOMER_NOTIFIED("Customer notification sent", false),
            SLA_BREACH_ALERT("SLA breach risk detected by ML system", false);


    private final String description;
    private final boolean changesStatus;

    EventType(String description, boolean changesStatus) {
        this.description = description;
        this.changesStatus = changesStatus;
    }

    public String getDescription() {
        return description;
    }

    public boolean isChangesStatus() {
        return changesStatus;
    }

    /**
     * Same defensive fromString as ShipmentStatus.
     * Kafka events from carrier integrations will send event types
     * as strings. We need safe conversion without crashing.
     */
    public static EventType fromString(String value) {
        if (value == null) return null;
        for (EventType type : EventType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * WHY THIS METHOD?
     *
     * The ML anomaly detector specifically cares about exception events.
     * Instead of the ML service hardcoding:
     *   if (type == WEATHER_DELAY || type == CUSTOMS_HOLD || type == LOST ...)
     *
     * It calls eventType.isException() and the knowledge
     * of "what counts as an exception" stays here, in one place.
     * If we add a new exception type later, we update ONE place.
     */
    public boolean isException() {
        return this == WEATHER_DELAY
                || this == ADDRESS_ISSUE
                || this == CUSTOMS_HOLD
                || this == DAMAGED
                || this == LOST
                || this == SECURITY_HOLD;
    }
}

