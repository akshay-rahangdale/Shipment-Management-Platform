-- V1__create_customers_table.sql
--
-- Flyway runs this exactly once, ever.
-- If you need to change this table later, you create V6__add_column_to_customers.sql
-- You NEVER edit this file after it has run in any environment.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- pgcrypto gives us gen_random_uuid() for generating UUIDs at the DB level.
-- We generate UUIDs in Java (GenerationType.UUID), but having this extension
-- available is good practice for any raw SQL inserts during testing/seeding.

CREATE TABLE customers (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    address    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_email UNIQUE (email)
);

-- Why name constraints explicitly?
-- Unnamed constraints get names like "customers_pkey" (auto-generated).
-- When a constraint violation error appears in logs, "pk_customers" is
-- immediately readable. "customers_pkey" requires you to look it up.

CREATE INDEX idx_customer_email ON customers (email);
CREATE INDEX idx_customer_phone ON customers (phone);

-- idx_customer_email might seem redundant given the UNIQUE constraint above.
-- The UNIQUE constraint does create an index, but naming it separately gives
-- us control. The UNIQUE constraint index is for enforcement — this explicit
-- index documents the query intent clearly for future developers.

COMMENT ON TABLE  customers           IS 'Shipment senders and recipients';
COMMENT ON COLUMN customers.email     IS 'Unique contact email, used for notifications';
COMMENT ON COLUMN customers.phone     IS 'E.164 format preferred e.g. +919876543210';