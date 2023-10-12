CREATE TABLE IF NOT EXISTS operation (
    id VARCHAR PRIMARY KEY,
    parentOperationId VARCHAR,
    name VARCHAR,
    date TIMESTAMP,
    type VARCHAR,
    description VARCHAR,
    state VARCHAR,
    instrumentUid VARCHAR,
    instrumentType VARCHAR,
    payment VARCHAR,
    price VARCHAR,
    commission VARCHAR,
    quantity VARCHAR,
    quantityRest VARCHAR,
    quantityDone VARCHAR
)