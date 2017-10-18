package api.databases;

import api.Exceptions;
import api.models.Forum;
import api.models.Thread;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static api.databases.Mappers.FORUM_ROW_MAPPER;
import static api.databases.Mappers.THREAD_ROW_MAPPER;
import static api.databases.Mappers.USER_ROW_MAPPER;

/**
 * Created by egor on 15.10.17.
 */

@Service
@Transactional
public class ThreadDAO {
    private JdbcTemplate jdbcTemplateObject;
    public ThreadDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
    }
    public Thread createThread(String slug, String author, String created, String forum, String message, String title) {

        String sql = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        try {
            jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, author);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundUser();
        }

        sql = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        try {
            Forum gotForum = jdbcTemplateObject.queryForObject(sql, FORUM_ROW_MAPPER, forum);
            forum = gotForum.getSlug();
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundForum();
        }

        sql = "INSERT INTO threads (slug, author, created, forum, message, title) VALUES (?, ?, ?::TIMESTAMPTZ , ?, ?, ?) RETURNING *";
        try {
            Thread thread = jdbcTemplateObject.queryForObject(sql, THREAD_ROW_MAPPER, slug, author, created, forum, message, title);
            return thread;
        } catch (DuplicateKeyException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }
    public Thread getThreadBySlug(String slug) {
        String sql = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)";
        Thread thread = null;
        try {
            thread = jdbcTemplateObject.queryForObject(sql,
                    THREAD_ROW_MAPPER, slug);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundThread();
        }
        return thread;
    }
    public Thread getThreadById(Integer id) {
        String sql = "SELECT * FROM threads WHERE id = ?";
        Thread thread;
        try {
            thread = jdbcTemplateObject.queryForObject(sql,
                    THREAD_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundThread();
        }
        return thread;
    }
    public List<Thread> getThreadsFromForum(String forum_slug, Integer limit, String since, Boolean desc) {
        String sql = null;
        if (since == null){
            sql = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) ORDER BY created ";
            if (desc != null && desc == true) {
                sql += "DESC ";
            }
        } else {
            if (desc != null && desc == true) {
                sql = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created <= ?::TIMESTAMPTZ ORDER BY created DESC ";
            } else {
                sql = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created >= ?::TIMESTAMPTZ ORDER BY created ";
            }
        }
        if (limit != null) {
            sql += "LIMIT ?";
        }

        List<Thread> threads = null;
        if(limit == null) {
            if(since == null) {
                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug);
            } else {
                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug, since);
            }
        } else {
            if(since == null) {
                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug, limit);
            } else {
                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug, since, limit);
            }
        }
        return threads;
    }
    public Boolean isCreated(Integer id) {
        String sql = "SELECT * FROM threads WHERE id = ?";
        Thread thread = null;
        try {
            thread = jdbcTemplateObject.queryForObject(sql,
                    THREAD_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }
    public void updateThread(String slug, String author, String created, String message, String title) {
        String sql = "UPDATE threads SET ";
        if (author != null) {
            sql += "author = ?, ";
        }
        if (created != null) {
            sql += "created = ?::TIMESTAMPTZ, ";
        }
        if (message != null) {
            sql += "message = ?, ";
        }
        if (title != null) {
            sql += "title = ?, ";
        }
        sql = sql.substring(0,sql.length() - 2);
        sql += " WHERE LOWER(slug) = LOWER(?)";

        try {
            if(author == null){
                if(created == null) {
                    if(message == null){
                        if(title == null){
                            throw new Exceptions.NotModified();
                        } else {
                            jdbcTemplateObject.update(sql, title, slug);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, message, slug);
                        } else {
                            jdbcTemplateObject.update(sql, message, title, slug);
                        }
                    }
                } else {
                    if(message == null){
                        if(title == null){
                            jdbcTemplateObject.update(sql, created, slug);
                        } else {
                            jdbcTemplateObject.update(sql, created, title, slug);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, created, message, slug);
                        } else {
                            jdbcTemplateObject.update(sql, created, message, title, slug);
                        }
                    }
                }
            } else {
                if(created == null) {
                    if(message == null){
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, slug);
                        } else {
                            jdbcTemplateObject.update(sql, title, slug);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, message, slug);
                        } else {
                            jdbcTemplateObject.update(sql, author, message, title, slug);
                        }
                    }
                } else {
                    if(message == null){
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, created, slug);
                        } else {
                            jdbcTemplateObject.update(sql, author, created, title, slug);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, created, message, slug);
                        } else {
                            jdbcTemplateObject.update(sql, author, created, message, title, slug);
                        }
                    }
                }
            }

        } catch (DuplicateKeyException e) {
            throw e;
        }
//        try {
//            jdbcTemplateObject.query(sql, USER_ROW_MAPPER);
//            //jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
//        } catch (NullPointerException e) {
//
//        }
    }
    public void updateThread(Integer id, String author, String created, String message, String title) {
        String sql = "UPDATE threads SET ";
        if (author != null) {
            sql += "author = ?, ";
        }
        if (created != null) {
            sql += "created = ?::TIMESTAMPTZ, ";
        }
        if (message != null) {
            sql += "message = ?, ";
        }
        if (title != null) {
            sql += "title = ?, ";
        }
        sql = sql.substring(0,sql.length() - 2);
        sql += "WHERE id = ? ";

        try {
            if(author == null){
                if(created == null) {
                    if(message == null){
                        if(title == null){
                            throw new Exceptions.NotModified();
                        } else {
                            jdbcTemplateObject.update(sql, title, id);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, message, id);
                        } else {
                            jdbcTemplateObject.update(sql, message, title, id);
                        }
                    }
                } else {
                    if(message == null){
                        if(title == null){
                            jdbcTemplateObject.update(sql, created, id);
                        } else {
                            jdbcTemplateObject.update(sql, created, title, id);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, created, message, id);
                        } else {
                            jdbcTemplateObject.update(sql, created, message, title, id);
                        }
                    }
                }
            } else {
                if(created == null) {
                    if(message == null){
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, id);
                        } else {
                            jdbcTemplateObject.update(sql, title, id);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, message, id);
                        } else {
                            jdbcTemplateObject.update(sql, author, message, title, id);
                        }
                    }
                } else {
                    if(message == null){
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, created, id);
                        } else {
                            jdbcTemplateObject.update(sql, author, created, title, id);
                        }
                    } else {
                        if(title == null){
                            jdbcTemplateObject.update(sql, author, created, message, id);
                        } else {
                            jdbcTemplateObject.update(sql, author, created, message, title, id);
                        }
                    }
                }
            }

        } catch (DuplicateKeyException e) {
            throw e;
        }
//        try {
//            jdbcTemplateObject.query(sql, USER_ROW_MAPPER);
//            //jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
//        } catch (NullPointerException e) {
//
//        }
    }
    public Boolean isCreated(String threadSlug) {
        String sql = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)";
        try {
             jdbcTemplateObject.queryForObject(sql,
                    THREAD_ROW_MAPPER, threadSlug);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }
    public Integer numOfThreads() {
        String sql = "SELECT COUNT(*) FROM threads";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }


//
//    public List<User> getUsersWithNickNameOrEmail(String nickname, String email) {
//        String sql = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
////        List<User> users = null;
////        try {
////            users = jdbcTemplateObject.queryForObject(sql,
////                    USERS_ROW_MAPPER, nickname, email);
////        } catch (EmptyResultDataAccessException e) {
////
////        }
////        return users;
//        List<User> users = null;
//        try {
//            users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, nickname, email);
//        } catch (EmptyResultDataAccessException e) {
//
//        }
//
//        return users;
//    }
}
