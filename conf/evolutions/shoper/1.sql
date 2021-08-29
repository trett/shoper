-- shoper schema

-- !Ups

CREATE TABLE users
(
    email    varchar(100) NOT NULL,
    password varchar(100) NOT NULL,
    name varchar(100),
    PRIMARY KEY (email)
);

CREATE TABLE purchases
(
    id      SERIAL PRIMARY KEY,
    name    varchar(255) NOT NULL,
    status  varchar(255) NOT NULL,
    comment varchar(255)
);

INSERT INTO users (email, password)
VALUES ('admin@server.local', '10000:KwdjA92VzGmsMeEXT2nshJCwoum+pBTkUFZvfFre8Ow=:m841hCA7LVe4luaQyZtCVg==');

-- !Downs

DROP TABLE users;
DROP TABLE purchases;