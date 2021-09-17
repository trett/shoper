-- Added login column

-- !Ups
ALTER TABLE users
    ADD COLUMN login VARCHAR(50);
UPDATE users SET login = SPLIT_PART(email, '@', 1);
DROP INDEX ux_email;
ALTER TABLE users ALTER COLUMN login SET NOT NULL;
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
CREATE UNIQUE INDEX ux_login ON users (login);

-- !Downs
DROP INDEX ux_login;
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users DROP COLUMN login;