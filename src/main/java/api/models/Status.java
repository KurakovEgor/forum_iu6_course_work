package api.models;

/**
 * Created by egor on 18.10.17.
 */
public class Status {
    private Integer post;
    private Integer user;
    private Integer thread;
    private Integer forum;

    public Status(Integer post, Integer user, Integer thread, Integer forum) {
        this.post = post;
        this.user = user;
        this.thread = thread;
        this.forum = forum;
    }

    public Integer getPost() {
        return post;
    }

    public void setPost(Integer post) {
        this.post = post;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public Integer getForum() {
        return forum;
    }

    public void setForum(Integer forum) {
        this.forum = forum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Status status = (Status) o;

        if (post != null ? !post.equals(status.post) : status.post != null) return false;
        if (user != null ? !user.equals(status.user) : status.user != null) return false;
        if (thread != null ? !thread.equals(status.thread) : status.thread != null) return false;
        return forum != null ? forum.equals(status.forum) : status.forum == null;
    }

    @Override
    public int hashCode() {
        int result = post != null ? post.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (thread != null ? thread.hashCode() : 0);
        result = 31 * result + (forum != null ? forum.hashCode() : 0);
        return result;
    }
}