package api.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by egor on 15.10.17.
 */
public class ForumRequest {

    private String slug;
    @NotNull
    private String title;
    @NotNull
    private String user;

    @JsonCreator
    public ForumRequest(@JsonProperty(value = "slug") String slug,
                       @JsonProperty(value = "title") String title,
                       @JsonProperty(value = "user") String user) {
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }
}
