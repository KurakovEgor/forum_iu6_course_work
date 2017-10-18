package db.models;

import javax.validation.constraints.NotNull;

/**
 * Created by egor on 15.10.17.
 */
public class Forum {

    @NotNull
    private String slug;

    @NotNull
    private String title;

    @NotNull
    private String user;

    private Integer threads;

    private Integer posts;

    public Integer getThreads() {
        return threads;
    }

    public Forum(String slug, String title, String user) {
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Forum forum = (Forum) o;

        if (!slug.equals(forum.slug)) return false;
        if (!title.equals(forum.title)) return false;
        return user.equals(forum.user);
    }

    @Override
    public int hashCode() {
        int result = slug.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
