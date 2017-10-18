CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL
    CONSTRAINT pk__users_id PRIMARY KEY,

  about VARCHAR,

  email VARCHAR
    CONSTRAINT uk__users_email UNIQUE
    CONSTRAINT nn__users_email NOT NULL,

  fullname VARCHAR
    CONSTRAINT nn__users_email NOT NULL,

  nickname VARCHAR
    CONSTRAINT uk__users_nickname UNIQUE
    CONSTRAINT nn__users_nickname NOT NULL
);