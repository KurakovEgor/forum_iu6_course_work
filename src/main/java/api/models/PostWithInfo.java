package api.models;

/**
 * Created by egor on 18.10.17.
 */
public class PostWithInfo {
    private Post post;
    private User author;
    private Thread thread;
    private Forum forum;

    public PostWithInfo(Post post) {
        this.post = post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PostWithInfo that = (PostWithInfo) o;

        if (!post.equals(that.post)) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (thread != null ? !thread.equals(that.thread) : that.thread != null) return false;
        return forum != null ? forum.equals(that.forum) : that.forum == null;
    }

    @Override
    public int hashCode() {
        int result = post.hashCode();
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (thread != null ? thread.hashCode() : 0);
        result = 31 * result + (forum != null ? forum.hashCode() : 0);
        return result;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }
}
