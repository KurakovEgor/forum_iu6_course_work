package api.databases;

import api.Exceptions;
import api.models.Post;
import api.models.PostWithInfo;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PostDAO(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
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
                    sql = "SELECT * FROM threads WHERE LOWER(slug) = LOWER(?)";
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
                if(post.getParent() != 0) {
                    sql = "SELECT * FROM posts WHERE id = ?";
                    Post parent = null;
                    try{
                        parent = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getParent());
                    } catch (EmptyResultDataAccessException e) {
                        throw new Exceptions.InvalidParrent();
                    }
                    if (!parent.getThread().equals(post.getThread())) {
                        throw new Exceptions.InvalidParrent();
                    }
                }
                if (post.getParent() != 0 && post.getParent() != null) {
                    sql = "UPDATE posts SET children = array_append(children, ?) WHERE id = ? RETURNING *";
                    jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getId(), post.getParent());
                }
                if (post.getCreated() == null) {
                    sql = "INSERT INTO posts (author, forum, is_editted, message, parent, thread_id) VALUES (?, ?, ?::BOOLEAN, ?, ?, ?) RETURNING *";
                    Post readyPost = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getAuthor(),
                            post.getForum(), post.getIsEdited(),
                            post.getMessage(), post.getParent(), post.getThread());
                    readyPosts.add(readyPost);
                } else {
                    sql = "INSERT INTO posts (author, created, forum, is_editted, message, parent, thread_id) VALUES (?, ?::TIMESTAMPTZ , ?, ?::BOOLEAN, ?, ?, ?) RETURNING *";
                    Post readyPost = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, post.getAuthor(),
                            post.getCreated(), post.getForum(), post.getIsEdited(),
                            post.getMessage(), post.getParent(), post.getThread());
                    readyPosts.add(readyPost);
                }
            }
            return readyPosts;
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }
    public Post getPost(Integer id) {
        String sql = "SELECT * FROM posts WHERE id = ?";
        Post post;
        try {
            post = jdbcTemplateObject.queryForObject(sql, POST_ROW_MAPPER, id);

        } catch (EmptyResultDataAccessException e) {
            throw new Exceptions.NotFoundPost();
        }
        return post;
    }
    public PostWithInfo getPostWithInfo(Integer id, Boolean needAuthor, Boolean needThread, Boolean needForum) {
        String sql;
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
    private List<Post> getPostFromThreadSortedByTreeOld(Thread thread, Integer limit, Integer since, Boolean desc) {
        Integer sinceId = null;
//        try {
//            sinceId = Integer.parseInt(since);
//        } catch (NumberFormatException e) {
//            sinceId = null;
//        }
        String sql;
        if (since == null) {
            sql = "SELECT * FROM posts WHERE thread_id = ? AND parent = 0 ORDER BY created, id ";
            if (desc != null && desc == true) {
                sql = "SELECT * FROM posts WHERE thread_id = ? ORDER BY created DESC, id DESC ";
            }
        } else {
            if(sinceId == null) {
                if (desc != null && desc == true) {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND created <= ?::TIMESTAMPTZ ORDER BY created DESC ";
                } else {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND created >= ?::TIMESTAMPTZ AND parent = 0 ORDER BY created ";
                }
            } else {
                if (desc != null && desc == true) {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND id < ? ORDER BY created DESC ";
                } else {
                    sql = "SELECT * FROM posts WHERE thread_id = ? AND id > ? AND parent = 0 ORDER BY created ";
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
                if( sinceId == null){
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since);
                } else {
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), sinceId);
                }
            }
        } else {
            if (since == null) {
                posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), limit);
            } else {
                if(sinceId ==  null){
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since, limit);
                } else {
                    posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), sinceId, limit);
                }
            }
        }
        List<Post> sortedPosts = new ArrayList<>();
        if (desc == null || desc == false) {

            for (Post post : posts) {
                sortedPosts.addAll(createTree(post));
                if (sortedPosts.size() > limit) {
                    while (sortedPosts.size() != limit) {
                        sortedPosts.remove(sortedPosts.size() - 1);
                    }
                    break;
                }
            }
        } else  {
//            for (Post post : posts) {
//                sortedPosts.addAll(createReversedTree(post));
//            }
//            Set<Integer> idOfPosts = new HashSet<>();
//            int i = 0;
//            while (i < sortedPosts.size()) {
//                if(!idOfPosts.contains(sortedPosts.get(i).getId()) && (limit == null || i < limit)){
//                    idOfPosts.add(i);
//                    ++i;
//                } else{
//                    sortedPosts.remove(i);
//                }
//            }
        }
        return sortedPosts;
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
            List<Post> rootPostWithChildren;
            Integer roots = 0;
            Boolean flag = false;
            for(Post post : posts) {
                if( since != null && post.getId() < since) {
                    flag = true;
                }
                if((since == null || flag) && post.getParent().equals(0)){
                    rootPostWithChildren = createReversedTree(post);
                    if (limit != null && roots < limit) {
                        resultPosts.addAll(rootPostWithChildren);
                        roots++;
                        if (roots == limit) {
                            break;
                        }
                    }
                }
            }
        }
        return resultPosts;

    }
    private List<Post> getPostFromThreadSortedByParentTreeOld(Thread thread, Integer limit, Integer since, Boolean desc) {
        String sql;
        if (since == null) {
            sql = "SELECT * FROM posts WHERE thread_id = ? AND parent = 0 ORDER BY created, id ";
            if (desc != null && desc == true) {
                sql = "SELECT * FROM posts WHERE thread_id = ? AND parent = 0 ORDER BY created DESC, id DESC ";
            }
        } else {
            if (desc != null && desc == true) {
                sql = "SELECT * FROM posts WHERE thread_id = ? AND id < ? AND parent = 0 ORDER BY created DESC ";
            } else {
                sql = "SELECT * FROM posts WHERE thread_id = ? AND id > ? AND parent = 0 ORDER BY created ";
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
                posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since);
            }
        } else {
            if (since == null) {
                posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), limit);
            } else {
                posts = jdbcTemplateObject.query(sql, POST_ROW_MAPPER, thread.getId(), since, limit);
            }
        }
        List<Post> sortedPosts = new ArrayList<>();
        if(desc == null || desc == false) {
            for (Post post : posts) {
                sortedPosts.addAll(createTree(post));
            }
        } else {
            for (Post post : posts) {
                sortedPosts.addAll(createReversedTree(post));
            }
        }
        return sortedPosts;
    }
    private List<Post> getChildrenPosts(Post post) {
        String sql = "SELECT * FROM posts WHERE parent = ? ORDER BY created, id";
        return jdbcTemplateObject.query(sql, POST_ROW_MAPPER, post.getId());
    }
    private List<Post> getReversedChildrenPosts(Post post) {
        String sql = "SELECT * FROM posts WHERE parent = ? ORDER BY created DESC, id DESC";
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
    private Map<Integer,List<Post>> splitPostsByParent(List<Post> sortedPosts) {
        Map<Integer,List<Post>> result = new HashMap<>();
        for(Post post : sortedPosts) {
            if(!result.containsKey(post.getId())) {
                result.put(post.getId(), new ArrayList<>());
            }
            result.get(post.getId()).add(post);
        }
        return result;
    }
    private List<Post> createReversedTree(Post post) {
        List<Post> posts = new ArrayList<>();

        for(Post childPost : getReversedChildrenPosts(post)) {
            posts.addAll(createReversedTree(childPost));
        }
        posts.add(post);
        return posts;
    }
    private List<Post> createReversedTree(List<Post> posts) {
        List<Post> sortedPosts = new ArrayList<>();
        for(Post post : posts) {
            sortedPosts.addAll(createReversedTree(post));
        }
        return sortedPosts;
    }
    public Integer numOfPosts() {
        String sql = "SELECT COUNT(*) FROM posts";
        return jdbcTemplateObject.queryForObject(sql, Integer.class);
    }

//    public Thread getThreadByTitle(String title) {
//        String sql = "SELECT * FROM threads WHERE LOWER(title) = LOWER(?)";
//        Thread thread = null;
//        try {
//            thread = jdbcTemplateObject.queryForObject(sql,
//                    THREAD_ROW_MAPPER, title);
//        } catch (EmptyResultDataAccessException e) {
//
//        }
//
//        return thread;
//    }
//
//    public List<Thread> getThreadsFromForum(String forum_slug, Integer limit, String since, Boolean desc) {
//        String sql = null;
//        if (since == null){
//            sql = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) ORDER BY created ";
//            if (desc != null && desc == true) {
//                sql += "DESC ";
//            }
//        } else {
//            if (desc != null && desc == true) {
//                sql = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created <= ?::TIMESTAMPTZ ORDER BY created DESC ";
//            } else {
//                sql = "SELECT * FROM threads WHERE LOWER(forum) = LOWER(?) AND created >= ?::TIMESTAMPTZ ORDER BY created ";
//            }
//        }
//        if (limit != null) {
//            sql += "LIMIT ?";
//        }
//
//        List<Thread> threads = null;
//        if(limit == null) {
//            if(since == null) {
//                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug);
//            } else {
//                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug, since);
//            }
//        } else {
//            if(since == null) {
//                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug, limit);
//            } else {
//                threads = jdbcTemplateObject.query(sql, THREAD_ROW_MAPPER, forum_slug, since, limit);
//            }
//        }
//        return threads;
//    }

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
