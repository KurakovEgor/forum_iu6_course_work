CREATE INDEX threads_slug ON threads (slug);
CREATE INDEX forums_slug ON forums (slug);
CREATE INDEX users_nickname ON users (nickname);
CREATE INDEX votes_thread ON votes (thread_id, nickname, voice);
CREATE INDEX posts_thread_created ON posts (thread_id, created);
CREATE INDEX posts_thread_id_created ON posts (thread_id, id, created);
CREATE INDEX posts_thread_id_children ON posts (thread_id, children);
CREATE INDEX threads_forum_created ON threads (forum, created);
CREATE INDEX forumsusers_forum_slug_nickname ON forums_users (forum_slug, nickname);
