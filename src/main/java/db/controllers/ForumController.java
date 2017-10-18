package db.controllers;

import db.Constants;
import db.Exceptions;
import db.databases.ForumDAO;
import db.databases.ThreadDAO;
import db.models.Forum;
import db.models.Thread;
import db.models.User;
import db.utils.MessageResponse;
import db.utils.ForumRequest;
import db.utils.ThreadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static db.controllers.UserController.userDAO;

/**
 * Created by egor on 15.10.17.
 */
@RestController
@RequestMapping(
        path = Constants.ApiConstants.FORUM_API_PATH,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class ForumController {
    @Autowired
    ForumDAO forumDAO;
    @Autowired
    ThreadDAO threadDAO;

    public ForumController() { }

    @PostMapping(path = "/create", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> createForum(@RequestBody ForumRequest request) {
        try {
            forumDAO.createForum(request.getSlug(), request.getTitle(), request.getUser());
        } catch (DuplicateKeyException e) {
            Forum forum = forumDAO.getForumBySlug(request.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forum);
        } catch (Exceptions.NotFoundUser e) {
            MessageResponse resp = new MessageResponse("\"Can't find user with nickname: "+request.getUser());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        Forum forum = forumDAO.getForumBySlug(request.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

    @GetMapping(path = "/{slug}/details", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getForum(@PathVariable String slug) {
        Forum forum;
        try {
            forum = forumDAO.getForumBySlug(slug);
        } catch ( Exceptions.NotFoundForum e) {
            forum = null;
        }
        if (forum == null) {
            MessageResponse resp = new MessageResponse("Can't find forum");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        return ResponseEntity.ok(forum);
    }
    @PostMapping(path = "/{forum_slug}/create", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> createThread(@PathVariable String forum_slug, @RequestBody ThreadRequest request) {
        Thread thread = null;
        try {
            thread = threadDAO.createThread(request.getSlug(), request.getAuthor(), request.getCreated(),
                    forum_slug, request.getMessage(), request.getTitle());
        } catch (DuplicateKeyException e) {
            thread = threadDAO.getThreadBySlug(request.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(thread);
        } catch (Exceptions.NotFoundUser e) {
            MessageResponse resp = new MessageResponse("\"Can't find user with nickname: "+request.getAuthor());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        } catch (Exceptions.NotFoundForum e) {
            MessageResponse resp = new MessageResponse("\"Can't find forum "+request.getForum());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(thread);
    }
    @GetMapping(path = "/{forum_slug}/threads", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> threadsFromForum(@PathVariable String forum_slug,
                                              @RequestParam(required = false, value = "limit") Integer limit,
                                              @RequestParam(required = false, value = "since") String since,
                                              @RequestParam(required = false, value = "desc") Boolean desc) {
        List<Thread> threads = threadDAO.getThreadsFromForum(forum_slug, limit, since, desc);
        if (threads.size() == 0) {
            Forum forum;
            try {
                forum = forumDAO.getForumBySlug(forum_slug);
            } catch (Exceptions.NotFoundForum e) {
                forum = null;
            }
            if(forum == null) {
                MessageResponse resp = new MessageResponse("Can't find forum by slug: "+ forum_slug);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }
        }
        return ResponseEntity.ok().body(threads);
    }
    @GetMapping(path = "/{forum_slug}/users", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> usersFromForum(@PathVariable String forum_slug,
                                              @RequestParam(required = false, value = "limit") Integer limit,
                                              @RequestParam(required = false, value = "since") String since,
                                              @RequestParam(required = false, value = "desc") Boolean desc) {
        try {
            forumDAO.getForumBySlug(forum_slug);
        } catch ( Exceptions.NotFoundForum e) {
            MessageResponse response = new MessageResponse("Can't find forum by slug: "+forum_slug);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        List<User> users = userDAO.getUsersFromForum(forum_slug, limit, since, desc);
        return ResponseEntity.ok(users);
    }

}

