CREATE TABLE files
(
    post_id   BIGINT       NOT NULL,
    filename VARCHAR(127) NOT NULL,
    mime_type VARCHAR(127) NOT NULL,
    content   BLOB(2097151) NOT NULL,
    PRIMARY KEY (post_id),
    FOREIGN KEY (post_id) REFERENCES posts (id)
);