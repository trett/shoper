-- id to user table
-- created_at to purchase
-- purchase to user reference

-- !Ups

ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users
    ADD COLUMN id SERIAL PRIMARY KEY;
CREATE UNIQUE INDEX ux_email ON users (email);

ALTER TABLE purchases
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE purchases
    ADD COLUMN user_id INTEGER;
ALTER TABLE purchases
    ADD CONSTRAINT fk_purchase_to_user_id FOREIGN KEY (user_id) REFERENCES users (id);

-- !Downs

ALTER TABLE purchases DROP COLUMN created_at;
ALTER TABLE purchases DROP CONSTRAINT fk_purchase_to_user_id;
ALTER TABLE purchases DROP COLUMN user_id;

ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users
    ADD PRIMARY KEY (email);
DROP INDEX ux_email;

