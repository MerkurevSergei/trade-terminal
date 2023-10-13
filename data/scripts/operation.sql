CREATE TABLE IF NOT EXISTS operation (
    id VARCHAR PRIMARY KEY,
    brokerAccountId VARCHAR,
    parentOperationId VARCHAR,
    name VARCHAR,
    date TIMESTAMP,
    type VARCHAR,
    description VARCHAR,
    state VARCHAR,
    instrumentUid VARCHAR,
    instrumentType VARCHAR,
    payment NUMERIC(20, 9),
    price NUMERIC(20, 9),
    commission NUMERIC(20, 9),
    quantity BIGINT,
    quantityRest BIGINT,
    quantityDone BIGINT
)