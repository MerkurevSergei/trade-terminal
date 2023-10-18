CREATE TABLE IF NOT EXISTS open_deal (
    operation_id VARCHAR PRIMARY KEY,
    quantity BIGINT,
    take_profit_price NUMERIC(20, 9)
)