DROP INDEX posts_thread;
CREATE INDEX posts_thread_parent_id ON posts (thread_id, parent, id);




