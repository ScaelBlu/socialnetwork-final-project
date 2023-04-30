CREATE TABLE personal_data (
    user_id BIGINT NOT NULL,
    real_name VARCHAR(255),
    date_of_birth DATE,
    city VARCHAR(127),
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);