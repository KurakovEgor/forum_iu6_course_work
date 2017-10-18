package db.controllers;

import db.Constants;
import db.databases.UserDAO;
import db.utils.MessageResponse;
import db.utils.UserRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import db.models.User;

import java.util.List;

@RestController
@RequestMapping(
        path = Constants.ApiConstants.USER_API_PATH,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class UserController {

    static UserDAO userDAO;

    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @PostMapping(path = "{nickname}/create", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> createUser(@PathVariable String nickname, @RequestBody UserRequest request) {
        try {
            userDAO.createUser(request.getFullname(), request.getEmail(), nickname, request.getAbout());
        } catch (DuplicateKeyException e) {
            List<User> users = userDAO.getUsersWithNickNameOrEmail(nickname, request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(users);
        }
        User user = userDAO.getUserByNickName(nickname);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping(path = "{nickname}/profile", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable String nickname, @RequestBody UserRequest request) {
        try {
            userDAO.updateUserWithNickName(request.getFullname(),request.getEmail(),nickname,request.getAbout());
        } catch (DuplicateKeyException e) {
            MessageResponse resp = new MessageResponse("Can't find user with id "+nickname);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }
        User user = userDAO.getUserByNickName(nickname);
        if (user == null) {
            MessageResponse resp = new MessageResponse("Can't find user with id "+nickname);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);

        }
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "{nickname}/profile", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getUser(@PathVariable String nickname) {
        final User user = userDAO.getUserByNickName(nickname);
        if (user == null) {
            MessageResponse resp = new MessageResponse("Can't find such user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        return ResponseEntity.ok(user);
    }


}