CREATE TABLE IF NOT EXISTS threads (
  id BIGSERIAL
    CONSTRAINT pk__threads_id PRIMARY KEY,

  author CITEXT
    CONSTRAINT nn__threads_author NOT NULL,

  created TIMESTAMPTZ,

  forum CITEXT,

  message CITEXT
    CONSTRAINT nn__threads_message NOT NULL,

  slug CITEXT
    CONSTRAINT uk__threads_slug UNIQUE,

  title CITEXT
    CONSTRAINT nn__threads_title NOT NULL,

  votes INT
    DEFAULT 0
);