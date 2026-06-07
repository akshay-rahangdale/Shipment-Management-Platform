package com.shipment.shipmentservice.model.enums;

public enum ShipmentStatus{
    PENDING("Shipment created, awaiting pickup"),
    PICKED_UP("Picked up by carrier"),
    IN_TRANSIT("In transit between facilities"),
    OUT_FOR_DELIVERY("Out for final delivery"),
    DELIVERED("Successfully delivered"),
    FAILED_DELIVERY("Delivery attempt failed"),
    RETURNED("Being returned to sender"),
    CANCELLED("Shipment cancelled"),
    EXCEPTION("Shipment encountered an exception");

    private final String description;

    /**
     * The enum constructor. Java calls this once per constant
     * when the class loads. You can't call it yourself —
     * that's what makes enums "closed" (fixed set of values).
     */
    ShipmentStatus(String description){
        this.description=description;
    }

    public String getDescription(){
        return description;
    }

    /**
     * WHY THIS METHOD?
     *
     * When Kafka sends a status as a JSON string "IN_TRANSIT",
     * you need to convert it back to the enum.
     * ShipmentStatus.fromString("IN_TRANSIT") → ShipmentStatus.IN_TRANSIT
     *
     * Without this, you'd call ShipmentStatus.valueOf("IN_TRANSIT")
     * which throws IllegalArgumentException on unknown values — crashing
     * the Kafka consumer. This version returns null instead, letting
     * YOU decide what to do with an unknown status.
     *
     * WHY name.equalsIgnoreCase(value)?
     * Defensive: "in_transit", "IN_TRANSIT", "In_Transit" all work.
     * Kafka payloads from external carriers may not match your casing.
     */
    public static ShipmentStatus fromStringtoEnum(String value){
        if(value==null) return null;
        for(ShipmentStatus status:ShipmentStatus.values()){
            if(status.name().equalsIgnoreCase(value)){
                return status;
            }
        }
        return null;
    }

    /**
     * Business logic belongs on the enum, not scattered in service classes.
     *
     * isTerminal() answers: "can this shipment receive more updates?"
     * DELIVERED, CANCELLED, RETURNED are end states — no further
     * status changes are valid. The service layer calls this before
     * processing a status update event.
     *
     * WHY HERE and not in ShipmentService?
     * If it were in ShipmentService, every other service that imports
     * ShipmentStatus would have to duplicate the logic or depend on
     * ShipmentService. The enum is self-contained.
     */
    public boolean isTerminal(){
        return this==DELIVERED || this==CANCELLED || this==RETURNED;
    }

    /**
     * isForwardProgress() checks if a transition makes sense.
     * A shipment shouldn't go from DELIVERED back to IN_TRANSIT.
     * This helps the service layer validate incoming Kafka events.
     */
    public boolean isForwardProgress(ShipmentStatus next){
        // Terminal states can't transition anywhere
        if (this.isTerminal()) return false;
        // Can't go back to PENDING from any active state
        if (next == PENDING) return false;
        return true;
    }
}