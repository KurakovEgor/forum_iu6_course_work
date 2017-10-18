package db.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;


/**
 * Created by egor on 14.10.17.
 */
public class UserRequest {

        private String about;
        @NotBlank
        private String email;
        @NotBlank
        private String fullname;

        @JsonCreator
        public UserRequest(@JsonProperty(value = "about") String about,
                           @JsonProperty(value = "email") String email,
                           @JsonProperty(value = "fullname") String fullname) {
            this.about = about;
            this.email = email;
            this.fullname = fullname;
        }

        public String getAbout() {
            return about;
        }

        public String getEmail() {
            return email;
        }

        public String getFullname() {
            return fullname;
        }

}
