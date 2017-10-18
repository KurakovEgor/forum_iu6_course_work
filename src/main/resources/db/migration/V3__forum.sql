CREATE TABLE IF NOT EXISTS forums (
  id BIGSERIAL
    CONSTRAINT pk__forums_id PRIMARY KEY,

  slug CITEXT
    CONSTRAINT uk__forums_slug UNIQUE
    CONSTRAINT nn__forums_slug NOT NULL,

  title CITEXT
    CONSTRAINT nn__forums_title NOT NULL,

  user_id BIGINT
    CONSTRAINT nn__forums_user NOT NULL
);