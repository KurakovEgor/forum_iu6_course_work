package api.databases;

import api.Exceptions;
import api.models.Forum;
import api.models.Thread;
import api.models.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static api.databases.Mappers.FORUM_ROW_MAPPER;
import static api.databases.Mappers.THREAD_ROW_MAPPER;
import static api.databases.Mappers.USER_ROW_MAPPER;

/**
 * Created by egor on 15.10.17.
 */

@Service
public class ThreadDAO {
    private JdbcTemplate jdbcTemplateObject;
    private static AtomicInteger numOfThreads = new AtomicInteger();
    public ThreadDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        try {
            numOfThreads.set(numOfThreads());
        } catch (BadSqlGrammarException ex) {
            numOfThreads.set(0);
        }
    }
    public Thread createThread(String slug, String author, String created, String forum, String message, String title) {

        String sql = "SELECT slug FROM forums WHERE slug = ?::citext";
        try {
            forum = jdbcTemplateObject.queryForObject(sql, String.class, forum);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundForum();
        }

        sql = "INSERT INTO threads (slug, author, created, forum, message, title) VALUES (?, ?, ?::TIMESTAMPTZ , ?, ?, ?) RETURNING *";
        Thread thread;
        try {
            thread = jdbcTemplateObject.queryForObject(sql, THREAD_ROW_MAPPER, slug, author, created, forum, message, title);
        } catch (DuplicateKeyException e) {
            throw e;
        }
        sql = "SELECT * FROM users WHERE nickname = ?::citext";
        User user;
        try {
            user = jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, thread.getAuthor());
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundUser();
        }
        sql = "INSERT INTO forums_users (forum_slug, id, nickname, fullname, email, about) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplateObject.update(sql, thread.getForum(), user.getId(), user.getNickname(),
                    user.getFullname(), user.getEmail(), user.getAbout());
        numOfThreads.incrementAndGet();
        ForumDAO.addThreadsNum(thread.getForum());
        return thread;
    }
    public Thread getThreadBySlug(String slug) {
        String sql = "SELECT * FROM threads WHERE slug = ?::citext";
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
        String sql;
        if (since == null){
            sql = "SELECT * FROM threads WHERE forum = ?::citext ORDER BY created ";
            if (desc != null && desc == true) {
                sql += "DESC ";
            }
        } else {
            if (desc != null && desc == true) {
                sql = "SELECT * FROM threads WHERE forum = ?::citext AND created <= ?::TIMESTAMPTZ ORDER BY created DESC ";
            } else {
                sql = "SELECT * FROM threads WHERE forum = ?::citext AND created >= ?::TIMESTAMPTZ ORDER BY created ";
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
        sql += " WHERE slug = ?::citext";

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
        String sql = "SELECT slug FROM threads WHERE slug = ?::citext";
        try {
             jdbcTemplateObject.queryForObject(sql,
                    String.class, threadSlug);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }
    public Integer numOfThreads() {
        String sql = "SELECT COUNT(*) FROM threads";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }

    public static Integer getNumOfThreads() {
        return numOfThreads.intValue();
    }

    public static void setNumOfThreads(Integer numOfThreads) {
        ThreadDAO.numOfThreads.set(numOfThreads);
    }
}
