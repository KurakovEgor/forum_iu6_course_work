package api.databases;

import api.Exceptions;
import api.models.Thread;
import api.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;

import static api.databases.Mappers.*;
import static api.databases.Mappers.VOTE_ROW_MAPPER;

/**
 * Created by egor on 14.10.17.
 */

@Service
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

        String sql = "SELECT id FROM threads WHERE slug = ?::citext";
        Integer threadId;
        try {
            threadId = jdbcTemplateObject.queryForObject(sql, Integer.class, threadSlug);
        } catch (EmptyResultDataAccessException e) {
            throw new DataIntegrityViolationException(null);
        }
        return vote(voice, threadId, nickname);
    }

    public Thread vote(Integer voice, Integer threadId, String nickname) {
        String sql = "SELECT voice FROM votes WHERE thread_id = ? AND nickname = ?";

        Thread thread;

        Integer oldVoice = null;
        try {
            oldVoice = jdbcTemplateObject.queryForObject(sql, Integer.class, threadId, nickname);
        } catch (EmptyResultDataAccessException e) {

        }
        if (oldVoice == null) {
            sql = "INSERT INTO votes (voice, nickname, thread_id) VALUES (?, ?, ?) RETURNING *";
            oldVoice = 0;
        } else {
            sql = "UPDATE votes SET voice = ? WHERE nickname = ? AND thread_id = ? RETURNING *";
        }
        try {
            jdbcTemplateObject.queryForObject(sql, VOTE_ROW_MAPPER, voice, nickname, threadId);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
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
