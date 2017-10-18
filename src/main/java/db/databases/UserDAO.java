package db.databases;

import db.models.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static db.databases.Mappers.USER_ROW_MAPPER;

/**
 * Created by egor on 14.10.17.
 */

@Service
@Transactional
public class UserDAO {

    private JdbcTemplate jdbcTemplateObject;

    public UserDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
    }



    public void createUser(String fullname, String email, String nickname, String about) {
        final String sql = "INSERT INTO users (about, email, fullname, nickname) VALUES (?, ?, ?, ?)";
        try {
            String str = new String();
            jdbcTemplateObject.queryForObject(sql, USER_ROW_MAPPER, about, email, fullname, nickname);
        } catch (DuplicateKeyException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {

        }
    }

    public void updateUserWithNickName(String fullname, String email, String nickname, String about) {
        //TODO:  like: insert into events (customer_id, ts ) values (:payload[customer_id], to_timestamp(:payload[ts], ' yyyy-mm-dd hh24:mi:ss'));
        if (fullname != null) {
            if (email != null) {
                if (about != null) {
                    String sql = "UPDATE users SET about = ?, email = ?, fullname = ? WHERE LOWER(nickname) = LOWER(?)";
                    try {
                        jdbcTemplateObject.update(sql, about, email, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                } else {
                    String sql = "UPDATE users SET email = ?, fullname = ? WHERE LOWER(nickname) = LOWER(?)";
                    try {
                        jdbcTemplateObject.update(sql, email, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                }
            } else {
                if (about != null) {
                    String sql = "UPDATE users SET about = ?, fullname = ? WHERE LOWER(nickname) = LOWER(?)";
                    try {
                        jdbcTemplateObject.update(sql, about, fullname, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                } else {
                    String sql = "UPDATE users SET fullname = ? WHERE LOWER(nickname) = LOWER(?)";
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
                    String sql = "UPDATE users SET about = ?, email = ? WHERE LOWER(nickname) = LOWER(?)";
                    try {
                        jdbcTemplateObject.update(sql, about, email, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                } else {
                    String sql = "UPDATE users SET email = ? WHERE LOWER(nickname) = LOWER(?)";
                    try {
                        jdbcTemplateObject.update(sql, email, nickname);
                    } catch (DuplicateKeyException e) {
                        throw e;
                    }
                }
            } else {
                if (about != null) {
                    String sql = "UPDATE users SET about = ? WHERE LOWER(nickname) = LOWER(?)";
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
        String sql = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        User user = null;
        try {
            user = jdbcTemplateObject.queryForObject(sql,
                    USER_ROW_MAPPER, nickname);
        } catch (EmptyResultDataAccessException e) {

        }

        return user;
    }

    public Boolean isCreated(String nickname){
        String sql = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        User user = null;
        try {
            user = jdbcTemplateObject.queryForObject(sql,
                    USER_ROW_MAPPER, nickname);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return true;
    }

    public List<User> getUsersWithNickNameOrEmail(String nickname, String email) {
        String sql = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
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

    public List<User> getUsersFromForum(String forum_slug, Integer limit, String since, Boolean desc) {
        String sql = "SELECT DISTINCT * FROM (" +
                "  SELECT\n" +
                "    u1.id       AS id," +
                "    u1.nickname AS nickname," +
                "    u1.email    AS email," +
                "    u1.fullname AS fullname," +
                "    u1.about    AS about" +
                "  FROM posts AS p" +
                "    JOIN users AS u1 ON (p.author = u1.nickname)" +
                "  WHERE LOWER(forum) = LOWER(?)" +
                "  UNION SELECT" +
                "          u2.id       AS id," +
                "          u2.nickname AS nickname," +
                "          u2.email    AS email," +
                "          u2.fullname AS fullname," +
                "          u2.about    AS about" +
                "        FROM threads AS t" +
                "          JOIN users AS u2 ON (t.author = u2.nickname)" +
                "        WHERE LOWER(forum) = LOWER(?) " +
                ") users ";
        if (since != null) {
            if (desc != null && desc == true) {
                sql += "WHERE LOWER(nickname) < LOWER(?) ";
            } else {
                sql += "WHERE LOWER(nickname) > LOWER(?) ";
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
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forum_slug, forum_slug);
            } else {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forum_slug, forum_slug, limit);
            }
        } else {
            if (limit == null) {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forum_slug, forum_slug, since);
            } else {
                users = jdbcTemplateObject.query(sql, USER_ROW_MAPPER, forum_slug, forum_slug, since, limit);
            }
        }
        return users;

    }

    public Integer numOfUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }
}
