package api.databases;

import api.Exceptions;
import api.models.Forum;
import api.models.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static api.databases.Mappers.FORUM_ROW_MAPPER;
import static api.databases.Mappers.USER_ROW_MAPPER;

/**
 * Created by egor on 15.10.17.
 */

@Service
public class ForumDAO {
    private JdbcTemplate jdbcTemplateObject;
    private static Integer numOfForums;

    public ForumDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        numOfForums = numOfForums();
    }

    public Forum createForum(String slug, String title, String user_nickname) {
        //TODO: Index user_nickname
        String sql = "SELECT nickname FROM users WHERE nickname = ?::citext";
        try {
            user_nickname = jdbcTemplateObject.queryForObject(sql, String.class, user_nickname);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundUser();
        }
        sql = "INSERT INTO forums (slug, title, user_nickname) VALUES (?, ?, ?) RETURNING *";
        try {
            Forum forum =  jdbcTemplateObject.queryForObject(sql, FORUM_ROW_MAPPER, slug, title, user_nickname);
            numOfForums++;
            return forum;
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }

    public Integer numOfForums() {
        String sql = "SELECT COUNT(*) FROM forums";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }

    public Forum getForumBySlug(String slug) {
        String sql = "SELECT * FROM forums WHERE slug = ?::citext";
        Forum forum = null;
        try {
            forum = jdbcTemplateObject.queryForObject(sql,
                    FORUM_ROW_MAPPER, slug);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundForum();
        }
        sql = "SELECT COUNT(*) FROM threads WHERE forum = ?";
        Integer threads = jdbcTemplateObject.queryForObject(sql, Integer.class, forum.getSlug());
        forum.setThreads(threads);
        sql = "SELECT COUNT(*) FROM posts WHERE forum = ?";
        Integer posts = jdbcTemplateObject.queryForObject(sql, Integer.class, forum.getSlug());
        forum.setPosts(posts);
        return forum;
    }

    public static Integer getNumOfForums() {
        return numOfForums;
    }

    public static void setNumOfForums(Integer numOfForums) {
        ForumDAO.numOfForums = numOfForums;
    }
}
