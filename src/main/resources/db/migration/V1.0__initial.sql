CREATE TABLE books
(
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(60)    NOT NULL,
    price        DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN        NOT NULL,
    rating       INT CHECK (rating BETWEEN 0 AND 5)
);