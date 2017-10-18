CREATE TABLE posts (
  id BIGSERIAL
    CONSTRAINT pk__posts_id PRIMARY KEY,

  author CITEXT
    CONSTRAINT nn__posts_author NOT NULL,

  created TIMESTAMPTZ
    DEFAULT now()
    CONSTRAINT  nn__posts_created NOT NULL,

  forum CITEXT,

  is_editted BOOLEAN
    DEFAULT FALSE,

  message CITEXT
    CONSTRAINT nn__posts_message NOT NULL,

  parent INT
    DEFAULT 0,

  children INT[],

  thread_id INT

);