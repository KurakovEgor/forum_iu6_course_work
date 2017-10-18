package db.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by egor on 15.10.17.
 */
public class PostRequest {

    @NotNull
    private String author;

    private String created;

    private String forum;

    private String message;

    private Integer parent;

    private String thread;

    @JsonCreator
    public PostRequest(@JsonProperty(value = "author") String author,
                       @JsonProperty(value = "created") String created,
                       @JsonProperty(value = "forum") String forum,
                       @JsonProperty(value = "message") String message,
                       @JsonProperty(value = "parent") Integer parent,
                       @JsonProperty(value = "thread") String thread) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.message = message;
        this.parent = parent;
        this.thread = thread;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreated() {
        return created;
    }

    public String getForum() {
        return forum;
    }

    public String getMessage() {
        return message;
    }

    public Integer getParent() {
        return parent;
    }

    public String getThread() {
        return thread;
    }
}
