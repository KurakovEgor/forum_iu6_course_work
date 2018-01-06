CREATE TABLE IF NOT EXISTS forums_users (
  forum_slug CITEXT,
  user_id INT,

  CONSTRAINT uk__forum_user UNIQUE (forum_slug, user_id)
);