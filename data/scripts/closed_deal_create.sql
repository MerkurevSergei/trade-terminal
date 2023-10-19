CREATE TABLE IF NOT EXISTS closed_deal (
    open_operation_id VARCHAR PRIMARY KEY,
    close_operation_id VARCHAR PRIMARY KEY,
    quantity BIGINT
)