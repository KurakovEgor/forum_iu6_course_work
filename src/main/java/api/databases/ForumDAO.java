package api.databases;

import api.Exceptions;
import api.models.Forum;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static api.databases.Mappers.FORUM_ROW_MAPPER;

/**
 * Created by egor on 15.10.17.
 */

@Service
public class ForumDAO {
    private JdbcTemplate jdbcTemplateObject;
    private static AtomicInteger numOfForums = new AtomicInteger();
    private static Map<String, AtomicInteger> forumsAndPostsNum = new ConcurrentHashMap<>();
    private static Map<String, AtomicInteger> forumsAndThreadsNum = new ConcurrentHashMap<>();

    public ForumDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        try {
            numOfForums.set(numOfForums());
            fillForumsAndPostsMap();
        } catch (BadSqlGrammarException ex) {
            numOfForums.set(0);
        }
    }
    private List<String> getForumSlugs() {
        String sql = "SELECT slug FROM forums";
        return jdbcTemplateObject.queryForList(sql, String.class);
    }

    private void fillForumsAndPostsMap() {
        List<String> forums = getForumSlugs();
        for (String forum : forums) {
            String sql = "SELECT COUNT(*) FROM posts WHERE forum = ?";
            Integer posts = jdbcTemplateObject.queryForObject(sql, Integer.class, forum);
            forumsAndPostsNum.put(forum, new AtomicInteger(posts));
        }
        for (String forum : forums) {
            String sql = "SELECT COUNT(*) FROM threads WHERE forum = ?";
            Integer threads = jdbcTemplateObject.queryForObject(sql, Integer.class, forum);
            forumsAndThreadsNum.put(forum, new AtomicInteger(threads));
        }
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
            numOfForums.incrementAndGet();
            forumsAndPostsNum.put(forum.getSlug(), new AtomicInteger(0));
            forumsAndThreadsNum.put(forum.getSlug(), new AtomicInteger(0));
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
        Integer threads = forumsAndThreadsNum.get(forum.getSlug()).intValue();
        forum.setThreads(threads);
        Integer posts = forumsAndPostsNum.get(forum.getSlug()).intValue();
        forum.setPosts(posts);
        return forum;
    }

    public static void addPostsNum(String forumSlug, Integer numOfPosts) {
        AtomicInteger num = forumsAndPostsNum.get(forumSlug);
        for(int i = 0; i < numOfPosts; ++i) {
            num.incrementAndGet();
        }
    }

    public static void addThreadsNum(String forumSlug) {
        AtomicInteger num = forumsAndThreadsNum.get(forumSlug);
        num.incrementAndGet();
    }

    public static Integer getNumOfForums() {
        return numOfForums.intValue();
    }

    public static void setNumOfForums(Integer numOfForums) {
        ForumDAO.numOfForums.set(numOfForums);
    }
}
