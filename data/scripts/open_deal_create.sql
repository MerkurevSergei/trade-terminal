CREATE TABLE IF NOT EXISTS open_deal (
    operation_id VARCHAR PRIMARY KEY,
    quantity BIGINT,
    take_profit_price NUMERIC(20, 9),
    CONSTRAINT open_deal_open_operation_id_fk FOREIGN KEY (operation_id) REFERENCES operation (id) ON DELETE CASCADE
)