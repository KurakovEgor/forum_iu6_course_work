package db.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by egor on 15.10.17.
 */
public class VoteRequest {

    private Integer voice;

    private String nickname;

    @JsonCreator
    public VoteRequest(@JsonProperty(value = "voice") Integer vote,
                       @JsonProperty(value = "nickname") String nickname) {
        this.voice = vote;
        this.nickname = nickname;
    }

    public Integer getVoice() {
        return voice;
    }

    public String getNickname() {
        return nickname;
    }
}

