CREATE TABLE users_to_users (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    PRIMARY KEY(user_id, friend_id)
);