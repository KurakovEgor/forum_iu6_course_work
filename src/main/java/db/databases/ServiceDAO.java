package db.databases;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by egor on 14.10.17.
 */

@Service
@Transactional
public class ServiceDAO {

    private JdbcTemplate jdbcTemplateObject;

    public ServiceDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
    }

    public void clear() {
        String sql = "TRUNCATE TABLE votes;" +
                "TRUNCATE TABLE posts;" +
                "TRUNCATE TABLE threads;" +
                "TRUNCATE TABLE forums;" +
                "TRUNCATE TABLE users;";
//        String sql = "DELETE FROM votes *";
//        jdbcTemplateObject.update(sql);
//        sql = "DELETE FROM posts *";
//        jdbcTemplateObject.update(sql);
//        sql = "DELETE FROM threads *";
//        jdbcTemplateObject.update(sql);
//        sql = "DELETE FROM forums *";
//        jdbcTemplateObject.update(sql);
//        sql = "DELETE FROM users *";
        jdbcTemplateObject.update(sql);
    }


}
