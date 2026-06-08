-- V2__create_carriers_table.sql

CREATE TABLE carriers (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    code          VARCHAR(10)  NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_carriers      PRIMARY KEY (id),
    CONSTRAINT uq_carriers_code UNIQUE (code)
);

CREATE INDEX idx_carrier_code   ON carriers (code);
CREATE INDEX idx_carrier_active ON carriers (active);

-- idx_carrier_active: the SLA monitor frequently queries
-- "give me all active carriers" to cross-check SLA policies.
-- Without this index, Postgres scans every carrier row.
-- With a boolean index, it's an immediate bitmap scan.

COMMENT ON TABLE  carriers        IS 'Shipping carrier companies (FedEx, DHL, etc.)';
COMMENT ON COLUMN carriers.code   IS 'Short unique identifier e.g. FEDEX, DHL, UPS';
COMMENT ON COLUMN carriers.active IS 'Soft delete flag — inactive carriers kept for historical shipments';