CREATE TABLE IF NOT EXISTS forums_users (
  forum_slug CITEXT,
  id INT,
  about citext,
  email citext,
  fullname citext,
  nickname citext,

  CONSTRAINT uk__forum_user UNIQUE (forum_slug, id)
);