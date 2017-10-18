package db.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by egor on 15.10.17.
 */
public class ThreadRequest {

    @NotNull
    private String author;

    @NotNull
    private String created;

    private String forum;

    private String message;

    @NotNull
    private String title;

    private String slug;

    @JsonCreator
    public ThreadRequest(@JsonProperty(value = "author") String author,
                         @JsonProperty(value = "created") String created,
                         @JsonProperty(value = "forum") String forum,
                         @JsonProperty(value = "message") String message,
                         @JsonProperty(value = "title") String title,
                         @JsonProperty(value = "slug") String slug) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.message = message;
        this.title = title;
        this.slug = slug;
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

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }
}
