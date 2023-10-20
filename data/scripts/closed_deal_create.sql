CREATE TABLE IF NOT EXISTS closed_deal (
    id VARCHAR PRIMARY KEY,
    open_operation_id VARCHAR,
    close_operation_id VARCHAR,
    open_date TIMESTAMP,
    close_date TIMESTAMP,
    quantity BIGINT,
    take_profit_price NUMERIC(20, 9),
    CONSTRAINT closed_deal_open_operation_id_fk FOREIGN KEY (open_operation_id) REFERENCES operation (id) ON DELETE CASCADE,
    CONSTRAINT closed_deal_close_operation_id_fk FOREIGN KEY (close_operation_id) REFERENCES operation (id) ON DELETE CASCADE
)

CREATE INDEX closed_deal_open_date_idx ON closed_deal(open_date);
CREATE INDEX closed_deal_close_date_idx ON closed_deal(close_date);