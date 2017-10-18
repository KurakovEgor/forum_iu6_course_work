package db.databases;

import db.Exceptions;
import db.models.Thread;
import db.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static db.databases.Mappers.*;
import static db.databases.Mappers.VOTE_ROW_MAPPER;

/**
 * Created by egor on 14.10.17.
 */

@Service
@Transactional
public class VoteDAO {

    private JdbcTemplate jdbcTemplateObject;

    @Autowired
    private  ThreadDAO threadDAO;
    @Autowired
    private UserDAO userDAO;

    public VoteDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
    }

    public Thread vote(Integer voice, String threadSlug, String nickname) {

        String sql = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)";
        Thread thread;
        try {
            thread = jdbcTemplateObject.queryForObject(sql, THREAD_ROW_MAPPER, threadSlug);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundThread();
        }
        return vote(voice, thread.getId(), nickname);
    }

    public Thread vote(Integer voice, Integer threadId, String nickname) {
        String sql = "SELECT * FROM votes WHERE thread_id = ? AND nickname = ?";

        Thread thread;

        Integer oldVoice = null;
        try {
            Vote vote = jdbcTemplateObject.queryForObject(sql, VOTE_ROW_MAPPER, threadId, nickname);
            oldVoice = vote.getVoice();
        } catch (EmptyResultDataAccessException e) {
            if (!userDAO.isCreated(nickname)) {
                throw new Exceptions.NotFoundUser();
            }
            if (!threadDAO.isCreated(threadId)) {
                throw new Exceptions.NotFoundThread();
            }
        }
        if (oldVoice == null) {
            sql = "INSERT INTO votes (voice, nickname, thread_id) VALUES (?, ?, ?) RETURNING *";
            oldVoice = 0;
        } else {
            sql = "UPDATE votes SET voice = ? WHERE nickname = ? AND thread_id = ? RETURNING *";
        }
        jdbcTemplateObject.queryForObject(sql, VOTE_ROW_MAPPER, voice, nickname, threadId);

        sql = "UPDATE threads SET votes = votes + (?) WHERE id = ? RETURNING *";
        Integer changeVoice = voice - oldVoice;
        try {
            thread = jdbcTemplateObject.queryForObject(sql, THREAD_ROW_MAPPER, changeVoice, threadId);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundThread();
        }
        return thread;
    }

//        try {
//            jdbcTemplateObject.query(sql, USER_ROW_MAPPER);
//            //jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
//        } catch (NullPointerException e) {
//
//        }
}
