CREATE TABLE IF NOT EXISTS share (
    figi VARCHAR PRIMARY KEY,
    ticker VARCHAR,
    class_code VARCHAR,
    isin VARCHAR,
    lot INTEGER,
    currency VARCHAR,
    short_enabled_flag BOOLEAN,
    name VARCHAR,
    exchange VARCHAR,
    country_of_risk VARCHAR,
    sector VARCHAR,
    uid VARCHAR,
    real_exchange VARCHAR,
    position_uid VARCHAR,
    first_1min_candle_date VARCHAR,
    first_1day_candle_date VARCHAR
)