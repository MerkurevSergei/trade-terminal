CREATE TABLE IF NOT EXISTS active_position_create (
    instrument_uid VARCHAR PRIMARY KEY,
    figi VARCHAR,
    ticker VARCHAR,
    name VARCHAR,
    date TIMESTAMP,
    price NUMERIC(20, 9),
    payment NUMERIC(20, 9),
    yield NUMERIC(20, 9),
    yieldRelative NUMERIC(20, 9),
    quantity BIGINT
)