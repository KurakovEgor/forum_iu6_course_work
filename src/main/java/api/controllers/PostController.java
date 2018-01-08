package api.controllers;

import api.Constants;
import api.Exceptions;
import api.databases.PostDAO;
import api.models.Post;
import api.models.PostWithInfo;
import api.utils.MessageResponse;
import api.utils.PostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by egor on 15.10.17.
 */
@RestController
@RequestMapping(
        path = Constants.ApiConstants.POST_API_PATH,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class PostController {
    @Autowired
    PostDAO postDAO;

    public PostController() {}

    @GetMapping(path = "/{post_id}/details", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getDetails(@PathVariable Integer post_id,
                                        @RequestParam(required = false, value = "related")List<String> request) {
        PostWithInfo postWithInfo;
        if (request == null) {
            try {
                postWithInfo = postDAO.getPostWithInfo(post_id, false,false,false);
            } catch (Exceptions.NotFoundPost e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post with id: "+post_id));
            }
        } else {
            try {
                if (request.contains("user")) {
                    if (request.contains("thread")) {
                        if (request.contains("forum")) {
                            postWithInfo = postDAO.getPostWithInfo(post_id, true, true, true);
                        } else {
                            postWithInfo = postDAO.getPostWithInfo(post_id, true, true, false);
                        }
                    } else {
                        if (request.contains("forum")) {
                            postWithInfo = postDAO.getPostWithInfo(post_id, true, false, true);
                        } else {
                            postWithInfo = postDAO.getPostWithInfo(post_id, true, false, false);
                        }
                    }
                } else {
                    if (request.contains("thread")) {
                        if (request.contains("forum")) {
                            postWithInfo = postDAO.getPostWithInfo(post_id, false, true, true);
                        } else {
                            postWithInfo = postDAO.getPostWithInfo(post_id, false, true, false);
                        }
                    } else {
                        if (request.contains("forum")) {
                            postWithInfo = postDAO.getPostWithInfo(post_id, false, false, true);
                        } else {
                            postWithInfo = postDAO.getPostWithInfo(post_id, false, false, false);
                        }
                    }
                }
            } catch (Exceptions.NotFoundPost e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post with id: "+ post_id));
            }
        }

        return ResponseEntity.ok(postWithInfo);
    }

    @PostMapping(path = "/{post_id}/details", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> updateDetails(@PathVariable Integer post_id, @RequestBody PostRequest request ) {
        Post post = null;
        try {
            postDAO.updatePost(post_id, request.getMessage(), request.getAuthor(), request.getCreated());
            post = postDAO.getPost(post_id);
        } catch (Exceptions.NotFoundPost e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post with id: "+post_id));
        } catch (Exceptions.NotModified e) {
            post = postDAO.getPost(post_id);
        }
        return ResponseEntity.ok(post);
    }
}
