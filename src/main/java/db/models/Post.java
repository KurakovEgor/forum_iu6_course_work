package db.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by egor on 15.10.17.
 */
public class Post {

    @NotNull
    private String author;

    private String created;

    private String forum;

    private Integer id;

    private Boolean isEdited;

    @NotNull
    private String message;

    private Integer parent;

    @JsonIgnore
    private String threadSlug;

    private Integer thread;

    @JsonIgnore
    private List<Integer> children;

    public Post(String author, String created, String forum, Boolean isEdited, String message, Integer parent, String thread_slug) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent;
        this.threadSlug = thread_slug;
    }

    public Post(String author, String created, String forum, Boolean isEdited, String message, Integer parent, Integer thread) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent;
        this.thread = thread;
    }

    public Post(String author, String created, String forum, Boolean isEdited, String message, Integer parent, Integer thread, List<Integer> children) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent;
        this.thread = thread;
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;
        Boolean result = id.equals(post.id);
        return result;
    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (forum != null ? forum.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (isEdited != null ? isEdited.hashCode() : 0);
        result = 31 * result + message.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (thread != null ? thread.hashCode() : 0);
        return result;
    }

    public List<Integer> getChildren() {
        return children;
    }

    public void setChildren(List<Integer> children) {
        this.children = children;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean editted) {
        isEdited = editted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public String getThreadSlug() {
        return threadSlug;
    }

    public void setThreadSlug(String threadSlug) {
        this.threadSlug = threadSlug;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
