package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

public class User {

    @NotNull
    private Integer id;

    @NotBlank
    private String fullname;

    @NotBlank
    private String email;

    private String nickname;

    private String about;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (fullname != null ? !fullname.equals(user.fullname) : user.fullname != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (nickname != null ? !nickname.equals(user.nickname) : user.nickname != null) return false;
        return about != null ? about.equals(user.about) : user.about == null;
    }

    @Override
    public int hashCode() {
        int result = fullname != null ? fullname.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        result = 31 * result + (about != null ? about.hashCode() : 0);
        return result;
    }

    @JsonCreator
    public User(@JsonProperty(value = "fullname", required = true) String fullname,
                @JsonProperty(value = "email", required = true) String email,
                @JsonProperty(value = "nickname") String nickname,
                @JsonProperty(value = "about") String about) {
        this.fullname = fullname;
        this.email = email;
        if (nickname != null) { this.nickname = nickname; }
        if (about != null) { this.about = about; }
    }

    public User(Integer id, String fullname, String email, String nickname, String about) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.nickname = nickname;
        this.about = about;
    }

    @JsonProperty
    public String getFullname() {
        return fullname;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public String getNickname() {
        return nickname;
    }

    @JsonProperty
    public String getAbout() {
        return about;
    }

    @JsonProperty
    public void setFullname(String fullname) { this.fullname = fullname; }

    @JsonProperty
    public void setEmail(String email) { this.email = email; }

    @JsonProperty
    public void setNickname(String nickname) { this.nickname = nickname; }

    @JsonProperty
    public void setAbout(String about) { this.about = about; }
}
