CREATE TABLE votes (

  nickname CITEXT
    CONSTRAINT nn__votes_nickname NOT NULL
    CONSTRAINT fk__votes_users REFERENCES users(nickname),

  voice INT
    CONSTRAINT nn__votes_voice NOT NULL,

  thread_id INT
    CONSTRAINT nn__votes_thread_id NOT NULL
    CONSTRAINT fk__votes_threads REFERENCES threads(id)
);

