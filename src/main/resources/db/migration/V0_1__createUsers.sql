CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(31) NOT NULL UNIQUE,
    email VARCHAR(127) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    registered_on TIMESTAMP NOT NULL,
    PRIMARY KEY(id)
)