package api.databases;

import api.models.Thread;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by egor on 14.10.17.
 */

@Service
public class ServiceDAO {

    private JdbcTemplate jdbcTemplateObject;

    private final PostDAO postDAO;
    private final ForumDAO forumDAO;
    private final ThreadDAO threadDAO;
    private final UserDAO userDAO;

    @Autowired
    public ServiceDAO(JdbcTemplate jdbcTemplateObject, ForumDAO forumDAO, ThreadDAO threadDAO, PostDAO postDAO, UserDAO userDAO) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
        this.userDAO = userDAO;
    }

    public void clear() {
        String sql = "TRUNCATE TABLE users, threads, forums, posts, votes, forums_users;";
        jdbcTemplateObject.update(sql);
        PostDAO.setNumOfPosts(0);
        ForumDAO.setNumOfForums(0);
        ThreadDAO.setNumOfThreads(0);
        UserDAO.setNumOfUsers(0);
    }


}
