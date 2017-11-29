package api.databases;

import api.Exceptions;
import api.models.Post;
import api.models.PostWithInfo;
import api.models.Thread;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static api.databases.Mappers.*;

/**
 * Created by egor on 15.10.17.
 */

@Service
@Transactional
public class PostDAO {
    private JdbcTemplate jdbcTemplateObject;
    private Connection connection;
    public PostDAO(JdbcTemplate jdbcTemplateObject, HikariDataSource hikariDataSource) {
        this.jdbcTemplateObject = jdbcTemplateObject;
        try {
            connection = hikariDataSource.getConnection();
        } catch (SQLException ignore) {};
    }

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ThreadDAO threadDAO;

    @Autowired
    private ForumDAO forumDAO;

    public List<Post> createPosts(List<Post> posts) {
        List<Post> readyPosts = new ArrayList<Post>();
        String sql = null;
        try {
            for( Post post : posts) {
                if (post.getParent() == null) {
                    post.setParent(0);
                }
                if(!userDAO.isCreated(post.getAuthor())) {
                    throw new Exceptions.NotFoundUser();
                }
                if (post.getThread() == null) {
                    sql = "SELECT * FROM threads WHERE slug = ?::citext";
                    Thread thread;
                    try {
                        thread = jdbcTemplateObject.queryForObject(sql, THREAD_ROW_MAPPER, post.getThreadSlug());
                    } catch (EmptyResultDataAccessException e) {
                        throw new Exceptions.NotFoundThread();
                    }
                    post.setThread(thread.getId());
                    post.setForum(thread.getForum());
                } else {
                    if (post.getForum() == null) {
                        Integer threadId = post.getThread();
                        try {
                            sql = "SELECT * FROM threads WHERE id = ?";
                            Thread thread = jdbcTemplateObject.queryForObject(sql, THREAD_ROW_MAPPER, threadId);
                            post.setForum(thread.getForum());
                        } catch (EmptyResultDataAccessException e) {
                            throw new Exceptions.NotFoundThread();
                        }
                    }
                }
                if(!threadDAO.isCreated(post.getThread())) {
                    throw new Exceptions.NotFoundThread();
                }
                List<Integer> path;
                if(!post.getParent().equals(0)) {
//                    sql = "SELECT * FROM posts WHERE id = ?";
                    Post parent = null;
                    try{
                        parent = getPost(post.getParent());
                        //parent = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getParent());
                    } catch (Exceptions.NotFoundPost e) {
                        throw new Exceptions.InvalidParrent();
                    }
                    path = parent.getChildren();
                    path.add(parent.getId());
                    if (!parent.getThread().equals(post.getThread())) {
                        throw new Exceptions.InvalidParrent();
                    }
                } else {
                    path = new ArrayList<>();
                    path.add(0);
                }
//                if (post.getParent() != 0 && post.getParent() != null) {
//                    sql = "UPDATE posts SET children = array_append(children, ?) WHERE id = ? RETURNING *";
//                    jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getId(), post.getParent());
//                }

                if (post.getCreated() == null) {
                    sql = "INSERT INTO posts (author, forum, is_editted, message, parent, thread_id, children) VALUES (?, ?, ?::BOOLEAN, ?, ?, ?, ?) RETURNING *";
                    Post readyPost = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getAuthor(),
                            post.getForum(), post.getIsEdited(),
                            post.getMessage(), post.getParent(), post.getThread(), createSqlArray(path));
                    readyPosts.add(readyPost);
                } else {
                    sql = "INSERT INTO posts (author, created, forum, is_editted, message, parent, thread_id, children) VALUES (?, ?::TIMESTAMPTZ , ?, ?::BOOLEAN, ?, ?, ?, ?) RETURNING *";
                    Post readyPost = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getAuthor(),
                            post.getCreated(), post.getForum(), post.getIsEdited(),
                            post.getMessage(), post.getParent(), post.getThread(), createSqlArray(path));
                    readyPosts.add(readyPost);
                }
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
        List<Post> posts = createTree(getRootPostsFromThread(thread));
        List<Post> resultPosts = new ArrayList<>();
        if (desc != null && desc == true) {
            Collections.reverse(posts);
        }
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
            List<Post> posts = createTree(getRootPostsFromThread(thread));
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
            List<Post> posts = createTree(getRootPostsFromThread(thread));
            Collections.reverse(posts);
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
    private List<Post> getChildrenPosts(Post post) {
        String sql = "SELECT * FROM posts WHERE parent = ? ORDER BY created, id";
        return jdbcTemplateObject.query(sql, POST_ROW_MAPPER, post.getId());
    }
    private List<Post> createTree(Post post) {
        List<Post> posts = new ArrayList<>();
        posts.add(post);
        for( Post childPost : getChildrenPosts(post)) {
            posts.addAll(createTree(childPost));
        }
        return posts;
    }
    private List<Post> createTree(List<Post> posts) {
        List<Post> sortedPosts = new ArrayList<>();
        for(Post post : posts) {
            sortedPosts.addAll(createTree(post));
        }
        return sortedPosts;
    }
    private List<Post> getRootPostsFromThread(Thread thread) {
        String sql = "SELECT * FROM posts WHERE thread_id = ? AND parent = 0 ORDER BY id";
        return jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId());
    }
    private Array createSqlArray(List<Integer> list) {
        Array intArray = null;
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

    //TODO: Index on parents
}
