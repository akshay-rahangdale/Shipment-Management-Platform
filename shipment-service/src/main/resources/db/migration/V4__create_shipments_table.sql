-- V4__create_shipments_table.sql
--
-- Must run AFTER V1 (customers) and V2 (carriers).
-- References both tables via foreign keys.

CREATE TABLE shipments (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    tracking_number      VARCHAR(20)    NOT NULL,
    sender_id            UUID           NOT NULL,
    recipient_id         UUID           NOT NULL,
    carrier_id           UUID           NOT NULL,
    status               VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    origin_address       TEXT           NOT NULL,
    destination_address  TEXT           NOT NULL,
    weight_kg            NUMERIC(8, 3)  NOT NULL,
    declared_value       NUMERIC(10, 2) NOT NULL,
    estimated_delivery   TIMESTAMP      NOT NULL,
    actual_delivery      TIMESTAMP,
    special_instructions TEXT,
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_shipments                 PRIMARY KEY (id),
    CONSTRAINT uq_shipments_tracking_number UNIQUE (tracking_number),
    CONSTRAINT fk_shipments_sender          FOREIGN KEY (sender_id)
        REFERENCES customers (id),
    CONSTRAINT fk_shipments_recipient       FOREIGN KEY (recipient_id)
        REFERENCES customers (id),
    CONSTRAINT fk_shipments_carrier         FOREIGN KEY (carrier_id)
        REFERENCES carriers (id),
    CONSTRAINT chk_shipments_weight         CHECK (weight_kg > 0),
    CONSTRAINT chk_shipments_value          CHECK (declared_value >= 0),
    CONSTRAINT chk_shipments_status         CHECK (status IN (
        'PENDING', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY',
        'DELIVERED', 'FAILED_DELIVERY', 'RETURNED', 'CANCELLED', 'EXCEPTION'
    ))
);

CREATE INDEX idx_shipment_tracking_number  ON shipments (tracking_number);
CREATE INDEX idx_shipment_status           ON shipments (status);
CREATE INDEX idx_shipment_sender           ON shipments (sender_id);
CREATE INDEX idx_shipment_recipient        ON shipments (recipient_id);
CREATE INDEX idx_shipment_carrier          ON shipments (carrier_id);
CREATE INDEX idx_shipment_estimated_delivery ON shipments (estimated_delivery);

-- Partial index: only indexes active (non-terminal) shipments.
-- The SLA monitor's most frequent query is "active shipments expiring soon."
-- A partial index on just those rows is smaller and faster than a full index.
-- Example: 10M total shipments but only 50K active → index has 50K entries not 10M.
CREATE INDEX idx_shipment_active_delivery ON shipments (estimated_delivery)
    WHERE status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED');

COMMENT ON TABLE  shipments                    IS 'Core shipment records — source of truth';
COMMENT ON COLUMN shipments.tracking_number    IS 'Customer-facing ID, format SHP-XXXXXXXXXX';
COMMENT ON COLUMN shipments.actual_delivery    IS 'NULL until shipment is delivered';
COMMENT ON COLUMN shipments.declared_value     IS 'Value in USD for customs and insurance';