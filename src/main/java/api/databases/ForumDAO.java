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

import static api.databases.Mappers.FORUM_ROW_MAPPER;
import static api.databases.Mappers.USER_ROW_MAPPER;

/**
 * Created by egor on 15.10.17.
 */

@Service
@Transactional
public class ForumDAO {
    private JdbcTemplate jdbcTemplateObject;
    public ForumDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
    }
    public void createForum(String slug, String title, String user_nickname) {

        String sql = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        User user = null;
        try {
            user = jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, user_nickname);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundUser();
        }
        sql = "INSERT INTO forums (slug, title, user_nickname) VALUES (?, ?, ?)";
        try {
            jdbcTemplateObject.queryForObject(sql, FORUM_ROW_MAPPER, slug, title, user.getNickname());
        } catch (DuplicateKeyException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {


        }
    }
    public Integer numOfForums() {
        String sql = "SELECT COUNT(*) FROM forums";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }
    public Forum getForumBySlug(String slug) {
        String sql = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        Forum forum = null;
        try {
            forum = jdbcTemplateObject.queryForObject(sql,
                    FORUM_ROW_MAPPER, slug);
        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundForum();
        }
        sql = "SELECT COUNT(*) from threads where forum = ?";
        Integer threads = jdbcTemplateObject.queryForObject(sql, Integer.class, forum.getSlug());
        forum.setThreads(threads);
        sql = "SELECT COUNT(*) from posts where forum = ?";
        Integer posts = jdbcTemplateObject.queryForObject(sql, Integer.class, forum.getSlug());
        forum.setPosts(posts);
        return forum;
    }


//    public void updateUserWithNickName(String fullname, String email, String nickname, String about) {
//        final String sql = "UPDATE users SET about = ?, email = ?, fullname = ? WHERE LOWER(nickname) = LOWER(?)";
//
//        try {
//            jdbcTemplateObject.update(sql, about, email, fullname, nickname);
//        } catch (DuplicateKeyException e) {
//            throw e;
//        }
////        try {
////            jdbcTemplateObject.query(sql, USER_ROW_MAPPER);
////            //jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
////        } catch (NullPointerException e) {
////
////        }
//    }
//

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
