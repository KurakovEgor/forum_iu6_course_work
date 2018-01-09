package api.databases;

import api.models.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.databases.Mappers.USER_ROW_MAPPER;

/**
 * Created by egor on 14.10.17.
 */

@Service
public class UserDAO {

    private JdbcTemplate jdbcTemplateObject;
    private static Integer numOfUsers;

    public UserDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        try {
            numOfUsers = numOfUsers();
        } catch (BadSqlGrammarException ex) {
            numOfUsers = 0;
        }
    }



    public User createUser(String fullname, String email, String nickname, String about) {
        final String sql = "INSERT INTO users (about, email, fullname, nickname) VALUES (?, ?, ?, ?) RETURNING id, nickname, fullname, email, about";
        User user;
        try {
            String str = new String();
            user =  jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
            numOfUsers++;
            return user;
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }

    public void updateUserWithNickName(String fullname, String email, String nickname, String about) {
        //TODO:  like: insert into events (customer_id, ts ) values (:payload[customer_id], to_timestamp(:payload[ts], ' yyyy-mm-dd hh24:mi:ss'));
        if (fullname != null) {
            if (email != null) {
                if (about != null) {
                    String sql = "UPDATE users SET about = ?, email = ?, fullname = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, about, email, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                } else {
                    String sql = "UPDATE users SET email = ?, fullname = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, email, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                }
            } else {
                if (about != null) {
                    String sql = "UPDATE users SET about = ?, fullname = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, about, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                } else {
                    String sql = "UPDATE users SET fullname = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                }
            }
        } else {
            if (email != null) {
                if (about != null) {
                    String sql = "UPDATE users SET about = ?, email = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, about, email, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                } else {
                    String sql = "UPDATE users SET email = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, email, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                }
            } else {
                if (about != null) {
                    String sql = "UPDATE users SET about = ? WHERE nickname = ?::citext";
                    try {
                        jdbcTemplateObject.update(sql, about, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                }
            }
        }
//        try {
//            jdbcTemplateObject.query(sql, USER_ROW_MAPPER);
//            //jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
//        } catch (NullPointerException e) {
//
//        }
    }

    public User getUserByNickName(String nickname) {
        String sql = "SELECT * FROM users WHERE nickname = ?::citext";
        User user = null;
        try {
            user = jdbcTemplateObject.queryForObject(sql,
                    USER_ROW_MAPPER, nickname);
        } catch (EmptyResultDataAccessException e) {

        }

        return user;
    }

    public Boolean isCreated(String nickname){
        String sql = "SELECT nickname FROM users WHERE nickname = ?::citext";
        try {
            jdbcTemplateObject.queryForObject(sql,
                    String.class, nickname);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }

    public List<User> getUsersWithNickNameOrEmail(String nickname, String email) {
        String sql = "SELECT * FROM users WHERE nickname = ?::citext OR email = ?::citext";
//        List<User> users = null;
//        try {
//            users = jdbcTemplateObject.queryForObject(sql,
//                    USERS_ROW_MAPPER, nickname, email);
//        } catch (EmptyResultDataAccessException e) {
//
//        }
//        return users;
        List<User> users = null;
        try {
            users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, nickname, email);
        } catch (EmptyResultDataAccessException e) {

        }

        return users;
    }

    public List<User> getUsersFromForum(String forumSlug, Integer limit, String since, Boolean desc) {
        String sql = "SELECT id, nickname, fullname, email, about FROM forums_users WHERE forum_slug = ?::citext ";
        if (since != null) {
            if (desc != null && desc == true) {
                sql += "AND nickname < ?::citext ";
            } else {
                sql += "AND nickname > ?::citext ";
            }
        }
        sql += "ORDER BY nickname ";
        if (desc != null && desc == true) {
            sql += "DESC ";
        }
        if (limit != null) {
            sql += "LIMIT ?";
        }
        List<User> users;
        if(since == null) {
            if (limit == null) {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forumSlug);
            } else {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forumSlug, limit);
            }
        } else {
            if (limit == null) {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forumSlug, since);
            } else {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forumSlug, since, limit);
            }
        }
        return users;
    }

    public Integer numOfUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }

    public static Integer getNumOfUsers() {
        return numOfUsers;
    }

    public static void setNumOfUsers(Integer numOfUsers) {
        UserDAO.numOfUsers = numOfUsers;
    }
}
