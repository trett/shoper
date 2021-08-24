-- shoper schema

-- !Ups

CREATE TABLE users
(
    email    varchar(100) NOT NULL,
    password varchar(100) NOT NULL,
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
VALUES ('admin', '10000:KwdjA92VzGmsMeEXT2nshJCwoum+pBTkUFZvfFre8Ow=:m841hCA7LVe4luaQyZtCVg==');

INSERT INTO purchases (id, name, status)
VALUES (1, 'apple', 'TODO');
INSERT INTO purchases (id, name, status, comment)
VALUES (2, 'orange', 'TODO', 'maybe');

-- !Downs

DROP TABLE users;
DROP TABLE purchases;