-- V5__create_shipment_events_table.sql
--
-- Must run AFTER V4 (shipments).
-- This is the highest-volume table — every scan, every update,
-- every exception writes a row here.

CREATE TABLE shipment_events (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    shipment_id UUID         NOT NULL,
    event_type  VARCHAR(50)  NOT NULL,
    description TEXT         NOT NULL,
    location    VARCHAR(255),
    occurred_at TIMESTAMP    NOT NULL,
    source      VARCHAR(20)  NOT NULL,
    metadata    TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_shipment_events          PRIMARY KEY (id),
    CONSTRAINT fk_shipment_events_shipment FOREIGN KEY (shipment_id)
        REFERENCES shipments (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_event_type CHECK (event_type IN (
        'SHIPMENT_CREATED', 'PICKED_UP', 'DEPARTED_FACILITY', 'ARRIVED_FACILITY',
        'OUT_FOR_DELIVERY', 'DELIVERED', 'DELIVERY_ATTEMPTED', 'RETURNED_TO_SENDER',
        'CANCELLED', 'CHECKPOINT_SCAN', 'GPS_UPDATE', 'WEATHER_DELAY',
        'ADDRESS_ISSUE', 'CUSTOMS_HOLD', 'DAMAGED', 'LOST', 'SECURITY_HOLD',
        'REROUTED', 'PRIORITY_UPGRADED', 'CUSTOMER_NOTIFIED', 'SLA_BREACH_ALERT'
    )),
    CONSTRAINT chk_event_source CHECK (source IN (
        'SYSTEM', 'API', 'CARRIER_SYSTEM', 'CARRIER_API',
        'GPS_DEVICE', 'MANUAL', 'ML_SYSTEM'
    ))
);

-- Three separate indexes matching the three query patterns
-- identified in ShipmentEventRepository
CREATE INDEX idx_event_shipment_id ON shipment_events (shipment_id);
CREATE INDEX idx_event_type        ON shipment_events (event_type);
CREATE INDEX idx_event_occurred_at ON shipment_events (occurred_at);

-- Composite index for the ML detector's most common query:
-- "all events for shipment X after timestamp T"
-- A composite index on (shipment_id, occurred_at) serves this
-- in a single index scan — faster than using idx_event_shipment_id
-- and then filtering by occurred_at separately.
CREATE INDEX idx_event_shipment_time ON shipment_events (shipment_id, occurred_at ASC);

-- Partial index for exception events only.
-- The ML model queries exception events frequently.
-- Exception events are ~5% of all events — a partial index
-- is 20x smaller than a full index on event_type.
CREATE INDEX idx_event_exceptions ON shipment_events (shipment_id, occurred_at)
    WHERE event_type IN (
        'WEATHER_DELAY', 'ADDRESS_ISSUE', 'CUSTOMS_HOLD',
        'DAMAGED', 'LOST', 'SECURITY_HOLD'
    );

COMMENT ON TABLE  shipment_events            IS 'Immutable audit log of all shipment lifecycle events';
COMMENT ON COLUMN shipment_events.metadata   IS 'JSON blob for event-specific extra data';
COMMENT ON COLUMN shipment_events.source     IS 'Which system generated this event';
COMMENT ON COLUMN shipment_events.occurred_at IS 'Real-world time of event, may differ from created_at';