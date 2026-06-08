-- V3__create_sla_policies_table.sql
--
-- Must run AFTER V2 because we reference carriers(id) via FK.
-- Flyway guarantees this by running migrations in version order.

CREATE TABLE sla_policies (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    carrier_id       UUID           NOT NULL,
    service_tier     VARCHAR(50)    NOT NULL,
    max_transit_days INTEGER        NOT NULL,
    penalty_per_hour NUMERIC(10, 2) NOT NULL,
    effective_from   TIMESTAMP      NOT NULL,
    effective_to     TIMESTAMP,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_sla_policies              PRIMARY KEY (id),
    CONSTRAINT fk_sla_policies_carrier      FOREIGN KEY (carrier_id)
        REFERENCES carriers (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_sla_max_transit_days     CHECK (max_transit_days BETWEEN 1 AND 30),
    CONSTRAINT chk_sla_penalty_non_negative CHECK (penalty_per_hour >= 0),
    CONSTRAINT chk_sla_effective_range      CHECK (
        effective_to IS NULL OR effective_to > effective_from
    )
);

CREATE INDEX idx_sla_carrier_tier ON sla_policies (carrier_id, service_tier);
CREATE INDEX idx_sla_effective    ON sla_policies (effective_from, effective_to);

COMMENT ON TABLE  sla_policies                IS 'SLA contracts per carrier and service tier';
COMMENT ON COLUMN sla_policies.effective_to   IS 'NULL means policy is open-ended with no expiry';
COMMENT ON COLUMN sla_policies.penalty_per_hour IS 'Financial penalty in USD per hour of breach';