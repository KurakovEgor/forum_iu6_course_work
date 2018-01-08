package api.databases;

import api.Exceptions;
import api.models.Post;
import api.models.PostWithInfo;
import api.models.Thread;
import api.models.User;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

import static api.databases.Mappers.*;

/**
 * Created by egor on 15.10.17.
 */

@Service
public class PostDAO {
    private JdbcTemplate jdbcTemplateObject;
    private Connection connection;
    private static Integer numOfPosts;
    private static Integer i = 0;
    public PostDAO(JdbcTemplate jdbcTemplateObject, HikariDataSource hikariDataSource) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        try {
            numOfPosts = numOfPosts();
        } catch (BadSqlGrammarException ex) {
            numOfPosts = 0;
        }
        try {
            connection = hikariDataSource.getConnection();
        } catch (SQLException ignore) { };
    }

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ThreadDAO threadDAO;

    @Autowired
    private ForumDAO forumDAO;

    public List<Post> createPosts(List<Post> posts) {
        i++;
        List<Post> readyPosts = new ArrayList<>();
        String sql = "";
        Set<Integer> users = new HashSet<>();
        try {
            for( Post post : posts) {
                if (post.getParent() == null) {
                    post.setParent(0);
                }

                User user = userDAO.getUserByNickName(post.getAuthor());

                if (user == null) {
                    throw new Exceptions.NotFoundUser();
                } else {
                    post.setAuthorId(user.getId());
                }
            }
            for(Post post : posts) {
                if (!post.getParent().equals(0)) {
                    Post parent;
                    try {
                        parent = getPost(post.getParent());
                    } catch (Exceptions.NotFoundPost e) {
                        throw new Exceptions.InvalidParrent();
                    }
                    if (!parent.getThread().equals(post.getThread())) {
                        throw new Exceptions.InvalidParrent();
                    }
                    post.setChildren(parent.getChildren());
                } else {
                    post.setChildren(null);
                }
            }
            String time;
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("MSK"));
            time = sdf.format(date);
            sql = "INSERT INTO posts (author, forum, is_editted, message, parent, thread_id, created, children) VALUES (?, ?, ?::BOOLEAN, ?, ?, ?, ?::TIMESTAMPTZ, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Post post : posts) {
                    users.add(post.getAuthorId());
                    ps.setString(1, post.getAuthor());
                    ps.setString(2, post.getForum());
                    ps.setBoolean(3, post.getIsEdited());
                    ps.setString(4, post.getMessage());
                    ps.setInt(5, post.getParent());
                    ps.setInt(6, post.getThread());
                    if (post.getCreated() == null) {
                        post.setCreated(time);
                    }
                    ps.setString(7, post.getCreated());
                    ps.setArray(8, createSqlArray(post.getChildren()));
                    ps.addBatch();
                }
                ps.executeBatch();



                ResultSet rs = ps.getGeneratedKeys();
                for(Post post : posts) {
                    if (rs.next()) {
                        post.setId((int)rs.getLong(1));
                    }
                    readyPosts.add(post);
                }

            } catch (SQLException ignore) {

            }
            sql = "INSERT INTO forums_users (forum_slug, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
                for (Integer user : users) {
                    ps.setObject(1, readyPosts.get(0).getForum());
                    ps.setObject(2, user);
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException ignore) {

            }
            numOfPosts += readyPosts.size();
            if (numOfPosts > 1499999) {
                jdbcTemplateObject.execute("VACUUM ANALYZE");
            }
            if (!readyPosts.isEmpty()) {
                ForumDAO.addPostsNum(readyPosts.get(0).getForum(), readyPosts.size());
            }
            return readyPosts;
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }
    public Post getPost(Integer id) {
        String sql = "SELECT " +
                "* FROM posts WHERE id = ?";
        Post post;
        try {
            post = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, id);

        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundPost();
        }
        return post;
    }
    public PostWithInfo getPostWithInfo(Integer id, Boolean needAuthor, Boolean needThread, Boolean needForum) {
        //TODO: Денормализовать всё что здесь есть и джойнить по id
        Post post = getPost(id);
        PostWithInfo result = new PostWithInfo(post);
        if (needAuthor) {
            result.setAuthor(userDAO.getUserByNickName(post.getAuthor()));
        }
        if (needThread) {
            result.setThread(threadDAO.getThreadById(post.getThread()));
        }
        if (needForum) {
            result.setForum(forumDAO.getForumBySlug(post.getForum()));
        }
        return result;
    }
    public void updatePost(Integer id, String message, String author, String created) {
        String sql = "UPDATE posts SET ";
        if (author != null) {
            sql += "author = ?, ";
        }
        if (created != null) {
            sql += "created = ?::TIMESTAMPTZ, ";
        }
        if (message != null) {
            sql += "message = ?, ";
        }
        sql += "is_editted = true WHERE id = ?";
        String oldAuthor, oldCreated, oldMessage;
        Post post = getPost(id);

        try {
            if(author == null){
                if(created == null) {
                    if(message == null){
                        throw new Exceptions.NotModified();
                    } else {
                        if (message.equals(post.getMessage())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, message, id);
                    }
                } else {
                    if(message == null){
                        if (message.equals(post.getCreated())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, created, id);
                    } else {
                        if (message.equals(post.getMessage()) && created.equals(post.getCreated())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, created, message, id);
                    }
                }
            } else {
                if(created == null) {
                    if(message == null){
                        if (author.equals(post.getAuthor())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, author, id);
                    } else {
                        if (author.equals(post.getAuthor()) && message.equals(post.getMessage())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, author, message, id);
                    }
                } else {
                    if(message == null){
                        if (author.equals(post.getAuthor()) && created.equals(post.getCreated())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, author, created, id);
                    } else {
                        if (author.equals(post.getAuthor()) && message.equals(post.getMessage()) && created.equals(post.getCreated())) {
                            throw new Exceptions.NotModified();
                        }
                        jdbcTemplateObject.update(sql, author, created, message, id);
                    }
                }
            }
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }
    public List<Post> getPostsFromThread(Thread thread, Integer limit, Integer since, String sort, Boolean desc) {
        if (sort != null && sort.equals("tree")) {
            return getPostFromThreadSortedByTree(thread, limit, since, desc);
        } else if ( sort != null && sort.equals("parent_tree")) {
            return getPostFromThreadSortedByParentTree(thread, limit, since, desc);
        } else {
            return getPostFromThreadSortedByFlat(thread, limit, since, desc);
        }
    }
    private List<Post> getPostFromThreadSortedByFlat(Thread thread, Integer limit, Integer since, Boolean desc) {
        String sql = null;

        if (since == null) {
            sql = "SELECT * FROM posts WHERE thread_id = ? ORDER BY created, id ";
            if (desc != null && desc == true) {
                sql = "SELECT * FROM posts WHERE thread_id = ? ORDER BY created DESC, id DESC ";
            }
        } else {
            if (since != null){
                if (desc != null && desc == true) {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND id < ? ORDER BY created DESC, id DESC ";
                } else {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND id > ? ORDER BY created, id ";
                }
            } else {
                if (desc != null && desc == true) {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND created <= ?::TIMESTAMPTZ ORDER BY created DESC, id DESC ";
                } else {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND created >= ?::TIMESTAMPTZ ORDER BY created, id ";
                }
            }
        }
        if (limit != null) {
            sql += "LIMIT ?";
        }

        List<Post> posts = null;
        if (limit == null) {
            if (since == null) {
                posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId());
            } else {
                if (since == null) {
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since);
                } else {
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since);
                }
            }
        } else {
            if (since == null) {
                posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), limit);
            } else {
                if (since == null) {
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since, limit);
                } else {
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since, limit);
                }
            }
        }
        return posts;

    }
    private List<Post> getPostFromThreadSortedByTree(Thread thread, Integer limit, Integer since, Boolean desc) {
        String sql = "SELECT * FROM posts WHERE thread_id = ? ORDER BY children";
        if (desc != null && desc == true) {
            sql += " DESC";
        }
        List<Post> posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId());
        List<Post> resultPosts = new ArrayList<>();
        Boolean achieveSince = (since == null) ? true : false;
        for (Post post : posts) {
            if (achieveSince) {
                resultPosts.add(post);
                if (limit != null && limit.equals(resultPosts.size())) {
                    break;
                }
            } else {
                if (post.getId().equals(since)) {
                    achieveSince = true;
                }
            }
        }
        return resultPosts;
    }
    private List<Post> getPostFromThreadSortedByParentTree(Thread thread, Integer limit, Integer since, Boolean desc) {
        List<Post> resultPosts = new ArrayList<>();
        if (desc == null || desc == false) {
            String sql = "SELECT * FROM posts WHERE thread_id = ? ORDER BY children";
            List<Post> posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId());
            Boolean achieveSince = (since == null) ? true : false;
            Boolean achieveSinceRoot = (since == null) ? true : false;
            Integer numOfRoot = 0;
            for (Post post : posts) {
                if (achieveSince) {
                    if (!achieveSinceRoot && post.getParent().equals(0)) {
                        achieveSinceRoot = true;
                    }
                    if (achieveSinceRoot) {
                        if (post.getParent().equals(0)) {
                            numOfRoot++;
                        }
                        if (limit != null && numOfRoot.equals(limit + 1)) {
                            break;
                        } else {
                            resultPosts.add(post);
                        }
                    }

                } else {
                    if (post.getId().equals(since)) {
                        achieveSince = true;
                    }
                }
            }
        } else {
            String sql = "SELECT * FROM posts WHERE thread_id = ? ORDER BY children DESC";
            List<Post> posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId());
            List<Post> rootPostWithChildren = null;
            Integer roots = 0;
            Boolean flag = true;
            Boolean achieveSince = (since == null) ? true : false;
            for(Post post : posts) {
                if( !achieveSince && post.getId() < since) {
                    achieveSince = true;
                } else if( !achieveSince ) {
                    if( post.getParent().equals(0)) {
                        rootPostWithChildren = null;
                    } else {
                        if (rootPostWithChildren == null) {
                            rootPostWithChildren = new ArrayList<>();
                        }
                        rootPostWithChildren.add(post);
                    }
                }

                if( achieveSince ) {

                    if (rootPostWithChildren == null) {
                        rootPostWithChildren = new ArrayList<>();
                    } else if (flag) {
                        flag = false;
                    }
                    rootPostWithChildren.add(post);
                    if(post.getParent().equals(0)) {
                        roots++;
                    }
                    if (limit != null && roots.equals(limit)) {
                        break;
                    }
                }
            }
            resultPosts = (rootPostWithChildren == null) ? new ArrayList<>() : rootPostWithChildren;
        }
        return resultPosts;

    }

    private Array createSqlArray(List<Integer> list) {
        Array intArray = null;
        if (list == null) {
            list = new ArrayList<>();
        }
        try {
            intArray = connection.createArrayOf("int4", list.toArray());
        } catch (SQLException ignore) {

        }
        return intArray;
    }
    public Integer numOfPosts() {
        String sql = "SELECT COUNT(*) FROM posts";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }

    public static Integer getNumOfPosts() {
        return numOfPosts;
    }

    public static void setNumOfPosts(Integer numOfPosts) {
        PostDAO.numOfPosts = numOfPosts;
    }

    //TODO: Index on parents
}
