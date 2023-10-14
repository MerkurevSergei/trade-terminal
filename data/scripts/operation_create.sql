CREATE TABLE IF NOT EXISTS operation (
    id VARCHAR PRIMARY KEY,
    broker_account_id VARCHAR,
    parent_operation_id VARCHAR,
    name VARCHAR,
    date TIMESTAMP,
    type VARCHAR,
    description VARCHAR,
    state VARCHAR,
    instrument_uid VARCHAR,
    instrument_type VARCHAR,
    payment NUMERIC(20, 9),
    price NUMERIC(20, 9),
    commission NUMERIC(20, 9),
    quantity BIGINT,
    quantityRest BIGINT,
    quantityDone BIGINT
)