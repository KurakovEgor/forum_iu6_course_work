package api.controllers;

import api.Constants;
import api.databases.*;
import api.models.Status;
import api.utils.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constants.ApiConstants.SERVICE_API_PATH,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class ServiceController {

    public ServiceController() {}

    @Autowired
    PostDAO postDAO;
    @Autowired
    ThreadDAO threadDAO;
    @Autowired
    ForumDAO forumDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    ServiceDAO serviceDAO;

    @GetMapping(path = "/status", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> returnStatus () {
        Status status = new Status(PostDAO.getNumOfPosts(),
                UserDAO.getNumOfUsers(), ThreadDAO.getNumOfThreads(),
                ForumDAO.getNumOfForums());
        return ResponseEntity.ok(status);
    }

    @PostMapping(path = "/clear", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> clear () {
        serviceDAO.clear();
        return ResponseEntity.ok(new MessageResponse("Successful"));
    }


}