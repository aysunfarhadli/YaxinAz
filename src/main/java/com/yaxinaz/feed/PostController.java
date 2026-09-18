package com.yaxinaz.feed;

import com.yaxinaz.feed.dto.CreatePostCommentRequest;
import com.yaxinaz.feed.dto.CreatePostRequest;
import com.yaxinaz.feed.dto.PostCommentResponse;
import com.yaxinaz.feed.dto.PostResponse;
import com.yaxinaz.feed.dto.UpdatePostRequest;
import com.yaxinaz.response.PagedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Community Feed")
public class PostController {

    private final PostService postService;

    @PostMapping("/api/communities/{communityId}/posts")
    public ResponseEntity<PostResponse> create(@PathVariable Long communityId, @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(communityId, request));
    }

    @GetMapping("/api/communities/{communityId}/posts")
    public ResponseEntity<PagedResponse<PostResponse>> list(
            @PathVariable Long communityId,
            @RequestParam(required = false) PostType postType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.listPosts(communityId, postType, pageable));
    }

    @GetMapping("/api/posts/{id}")
    public ResponseEntity<PostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PatchMapping("/api/posts/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/posts/{id}/pin")
    public ResponseEntity<PostResponse> pin(@PathVariable Long id, @RequestParam boolean pinned) {
        return ResponseEntity.ok(postService.setPinned(id, pinned));
    }

    @PostMapping("/api/posts/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        postService.likePost(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/posts/{id}/like")
    public ResponseEntity<Void> unlike(@PathVariable Long id) {
        postService.unlikePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/posts/{id}/comments")
    public ResponseEntity<PostCommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CreatePostCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addComment(id, request));
    }

    @GetMapping("/api/posts/{id}/comments")
    public ResponseEntity<PagedResponse<PostCommentResponse>> listComments(
            @PathVariable Long id, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.listComments(id, pageable));
    }
}
