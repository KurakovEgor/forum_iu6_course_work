package db.models;

import javax.validation.constraints.NotNull;

/**
 * Created by egor on 15.10.17.
 */
public class Vote {
    @NotNull
    private String nickname;

    @NotNull
    private Integer threadId;

    @NotNull
    private Integer voice;

    public Vote(String nickname, Integer threadId, Integer voice) {
        this.nickname = nickname;
        this.threadId = threadId;
        this.voice = voice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vote vote = (Vote) o;

        if (!nickname.equals(vote.nickname)) return false;
        if (!threadId.equals(vote.threadId)) return false;
        return voice.equals(vote.voice);
    }

    @Override
    public int hashCode() {
        int result = nickname.hashCode();
        result = 31 * result + threadId.hashCode();
        result = 31 * result + voice.hashCode();
        return result;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getThreadId() {
        return threadId;
    }

    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }
}
