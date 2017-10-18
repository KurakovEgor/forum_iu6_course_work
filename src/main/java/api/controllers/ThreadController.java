package api.controllers;

import api.Constants;
import api.Exceptions;
import api.databases.PostDAO;
import api.databases.ThreadDAO;
import api.databases.VoteDAO;
import api.models.Post;
import api.models.Thread;
import api.utils.MessageResponse;
import api.utils.PostRequest;
import api.utils.ThreadRequest;
import api.utils.VoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by egor on 15.10.17.
 */
@RestController
@RequestMapping(
        path = Constants.ApiConstants.THREAD_API_PATH,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class ThreadController {
    @Autowired
    ThreadDAO threadDAO;

    @Autowired
    PostDAO postDAO;

    @Autowired
    VoteDAO voteDAO;

    public ThreadController() {}

    @PostMapping(path = "/{thread_slug_or_id}/create", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> createPosts(@PathVariable String thread_slug_or_id, @RequestBody List<PostRequest> request) {
        List<Post> posts = new ArrayList<Post>();
        for(PostRequest postRequest : request ) {
            Post post;
            try {
                Integer thread = Integer.parseInt(thread_slug_or_id);
                post = new Post(postRequest.getAuthor(), postRequest.getCreated(),
                        postRequest.getForum(), false, postRequest.getMessage(),
                        postRequest.getParent(), thread);
            } catch (NumberFormatException e) {
                post = new Post(postRequest.getAuthor(), postRequest.getCreated(),
                        postRequest.getForum(), false, postRequest.getMessage(),
                        postRequest.getParent(), thread_slug_or_id);
            }
            posts.add(post);
        }
        try {
            if(posts.size()==0) {
                try {
                    Integer thread = Integer.parseInt(thread_slug_or_id);
                    if(!threadDAO.isCreated(thread)) {
                        throw new Exceptions.NotFoundThread();
                    }
                } catch (NumberFormatException e) {
                    if(!threadDAO.isCreated(thread_slug_or_id)) {
                        throw new Exceptions.NotFoundThread();
                    }
                }
            }
            posts = postDAO.createPosts(posts);
        } catch (DuplicateKeyException e) {

        } catch (Exceptions.InvalidParrent e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Parent post was created in another thread"));
        } catch (Exceptions.NotFoundUser e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post author by nickname"));
        } catch (Exceptions.NotFoundThread e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post thread by id"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @GetMapping(path = "/{thread_slug_or_id}/details", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getDetails(@PathVariable String thread_slug_or_id) {
        Thread thread;
        try {
            try {
                Integer threadId = Integer.parseInt(thread_slug_or_id);
                thread = threadDAO.getThreadById(threadId);
            } catch (NumberFormatException e) {
                thread = threadDAO.getThreadBySlug(thread_slug_or_id);
            }
        } catch (Exceptions.NotFoundThread e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread with slug" + thread_slug_or_id));
        }
        return ResponseEntity.ok().body(thread);
    }

    @PostMapping(path = "/{thread_slug_or_id}/details", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> updateThread(@PathVariable String thread_slug_or_id, @RequestBody ThreadRequest request ) {
        Thread thread = null;
        try {
            try {
                try {
                    Integer threadId = Integer.parseInt(thread_slug_or_id);
                    threadDAO.updateThread(threadId, request.getAuthor(), request.getCreated(), request.getMessage(), request.getTitle());
                    thread = threadDAO.getThreadById(threadId);
                } catch (NumberFormatException e) {
                    threadDAO.updateThread(thread_slug_or_id, request.getAuthor(), request.getCreated(), request.getMessage(), request.getTitle());
                    thread = threadDAO.getThreadBySlug(thread_slug_or_id);
                }
            } catch (Exceptions.NotModified e) {
                try {
                    Integer threadId = Integer.parseInt(thread_slug_or_id);
                    thread = threadDAO.getThreadById(threadId);
                } catch (NumberFormatException e1) {
                    thread = threadDAO.getThreadBySlug(thread_slug_or_id);
                }
            }
        } catch (Exceptions.NotFoundThread e) {
            thread = null;
        }
        if (thread == null) {
            MessageResponse resp = new MessageResponse("Can't find forum");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        return ResponseEntity.ok(thread);
    }

    @GetMapping(path = "/{thread_slug_or_id}/posts", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> postsFromThread(@PathVariable String thread_slug_or_id,
                                              @RequestParam(required = false, value = "limit") Integer limit,
                                              @RequestParam(required = false, value = "since") Integer since,
                                              @RequestParam(required = false, value = "sort") String sort,
                                              @RequestParam(required = false, value = "desc") Boolean desc) {
        Thread thread;
        List<Post> posts;
        try {
            try {
                Integer threadId = Integer.parseInt(thread_slug_or_id);
                thread = threadDAO.getThreadById(threadId);
            } catch (NumberFormatException e) {
                thread = threadDAO.getThreadBySlug(thread_slug_or_id);
            }
            posts = postDAO.getPostsFromThread(thread, limit, since, sort, desc);
        } catch (Exceptions.NotFoundThread e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread by slug: "+thread_slug_or_id));
        }
        return ResponseEntity.ok(posts);
    }

    @PostMapping(path = "/{thread_slug_or_id}/vote", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> vote(@PathVariable String thread_slug_or_id, @RequestBody VoteRequest request) {
        Thread thread;
        try {
            try {
                Integer threadId = Integer.parseInt(thread_slug_or_id);
                thread = voteDAO.vote(request.getVoice(), threadId, request.getNickname());
            } catch (NumberFormatException e) {
                thread = voteDAO.vote(request.getVoice(), thread_slug_or_id, request.getNickname());
            }
        } catch (Exceptions.NotFoundThread e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread by slug: "+thread_slug_or_id));
        } catch (Exceptions.NotFoundUser e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post author by nickname"));
        }
        return ResponseEntity.ok(thread);
    }

//    @PostMapping(path = "/create", consumes = MediaType.ALL_VALUE)
//    public ResponseEntity<?> createForum(@RequestBody ForumRequest request) {
//        try {
//            threadDAO.createForum(request.getSlug(), request.getTitle(), request.getUser());
//        } catch (DuplicateKeyException e) {
//            Forum forum = threadDAO.getForumBySlug(request.getSlug());
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(forum);
//        } catch (api.Exceptions.NotFoundUser e) {
//            MessageResponse resp = new MessageResponse("\"Can't find user with nickname: "+request.getUser());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
//        }
//        Forum forum = threadDAO.getForumBySlug(request.getSlug());
//        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
//    }
//
//    @GetMapping(path = "/{slug}/details", consumes = MediaType.ALL_VALUE)
//    public ResponseEntity<?> getForum(@PathVariable String slug) {
//        final Thread thread = threadDAO.getThreadBySlug(slug);
//        if (thread == null) {
//            MessageResponse resp = new MessageResponse("Can't find forum");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
//        }
//        return ResponseEntity.ok(thread);
//    }

    }
