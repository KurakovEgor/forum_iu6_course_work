package db.databases;

import db.models.Forum;
import db.models.Post;
import db.models.Thread;
import db.models.User;
import db.models.Vote;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by egor on 15.10.17.
 */
public class Mappers {
    static final RowMapper<User> USER_ROW_MAPPER = (res, num) -> new User(
            Integer.parseInt(res.getString("id")),
            res.getString("fullname"),
            res.getString("email"),
            res.getString("nickname"),
            res.getString("about")
    );

    static final RowMapper<Forum> FORUM_ROW_MAPPER = (res, num) -> new Forum(
            res.getString("slug"),
            res.getString("title"),
            res.getString("user_nickname")
    );

    static final RowMapper<Thread> THREAD_ROW_MAPPER = (res, num) -> {
        String author = res.getString("author");
        Timestamp created = res.getTimestamp("created");
        String forum = res.getString("forum");
        Integer id = Integer.parseInt(res.getString("id"));
        String message = res.getString("message");
        String slug = res.getString("slug");
        String title = res.getString("title");
        Integer votes = Integer.parseInt(res.getString("votes"));

        return new Thread(author,
                (created == null) ? null : created.toInstant().toString(),
                forum,id,message,slug,title,votes);
    };

    static final RowMapper<Post> POST_ROW_MAPPER = (res, num) -> {
        String author = res.getString("author");
        Timestamp created = res.getTimestamp("created");
        String forum = res.getString("forum");
        Integer id = Integer.parseInt(res.getString("id"));
        Boolean isEditted = res.getBoolean("is_editted");
        String message = res.getString("message");
        Integer parentId = res.getInt("parent");
        Integer thread = res.getInt("thread_id");
        List<Integer> children;
        try {
            children= new ArrayList<Integer>(Arrays.asList((Integer[]) res.getArray("children").getArray()));
        } catch (NullPointerException e) {
            children = new ArrayList<Integer>();
        }
        Post post =  new Post(author,
                    (created == null) ? null : created.toInstant().toString(),
                    forum, isEditted, message, parentId, thread, children);
        post.setId(id);
        return post;
    };

    static final RowMapper<Vote> VOTE_ROW_MAPPER = (res, num) -> new Vote(
            res.getString("nickname"),
            res.getInt("thread_id"),
            res.getInt("voice"));
}
