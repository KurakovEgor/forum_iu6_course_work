CREATE INDEX threads_slug ON threads (slug);
CREATE INDEX users_nickname ON users (nickname);
CREATE INDEX forums_slug ON forums (slug);
CREATE INDEX threads_forum ON threads (forum);
CREATE INDEX threads_forum_created ON threads (forum, created);


