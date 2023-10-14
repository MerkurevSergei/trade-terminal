CREATE TABLE IF NOT EXISTS available_share (
    uid VARCHAR PRIMARY KEY,
    figi VARCHAR,
    ticker VARCHAR,
    name VARCHAR,
    lot INTEGER,
    currency VARCHAR,
    exchange VARCHAR,
    real_exchange VARCHAR,
    short_enabled_flag BOOLEAN,
    country_of_risk VARCHAR,
    sector VARCHAR,
    class_code VARCHAR,
    share_type VARCHAR,
    first_1min_candle_date TIMESTAMP,
    first_1day_candle_date TIMESTAMP
)