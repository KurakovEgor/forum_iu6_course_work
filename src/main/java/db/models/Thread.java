package db.models;

import javax.validation.constraints.NotNull;

/**
 * Created by egor on 15.10.17.
 */
public class Thread {

    @NotNull
    private String author;

    private String created;

    private String forum;

    private Integer id;

    @NotNull
    private String message;

    private String slug;

    private String title;

    private Integer votes;

    public Thread(String author, String created, String forum,
                  Integer id, String message, String slug,
                  String title, Integer votes) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Thread thread = (Thread) o;

        if (!author.equals(thread.author)) return false;
        if (created != null ? !created.equals(thread.created) : thread.created != null) return false;
        if (forum != null ? !forum.equals(thread.forum) : thread.forum != null) return false;
        if (id != null ? !id.equals(thread.id) : thread.id != null) return false;
        if (!message.equals(thread.message)) return false;
        if (slug != null ? !slug.equals(thread.slug) : thread.slug != null) return false;
        if (!title.equals(thread.title)) return false;
        return votes != null ? votes.equals(thread.votes) : thread.votes == null;
    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (forum != null ? forum.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + message.hashCode();
        result = 31 * result + (slug != null ? slug.hashCode() : 0);
        result = 31 * result + title.hashCode();
        result = 31 * result + (votes != null ? votes.hashCode() : 0);
        return result;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
