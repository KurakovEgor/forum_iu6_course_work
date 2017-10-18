package db.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by egor on 15.10.17.
 */
public class PostWithInfoRequest {

    @NotNull
    private Integer id;

    private List<String> related;

    @JsonCreator
    public PostWithInfoRequest(@JsonProperty(value = "id") Integer id,
                               @JsonProperty(value = "related") List<String> related ) {
        this.id = id;
        this.related = related;
    }

    public Integer getId() {
        return id;
    }

    public List<String> getRelated() {
        return related;
    }
}
