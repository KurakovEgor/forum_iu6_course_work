ALTER TABLE forums DROP COLUMN user_id;
ALTER TABLE forums ADD COLUMN user_nickname CITEXT; -- CONSTRAINT fk__forums_user REFERENCES users(nickname);