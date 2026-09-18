package com.yaxinaz.feed;

import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.exception.PostNotFoundException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.feed.dto.CreatePostCommentRequest;
import com.yaxinaz.feed.dto.CreatePostRequest;
import com.yaxinaz.feed.dto.PostCommentResponse;
import com.yaxinaz.feed.dto.PostResponse;
import com.yaxinaz.feed.dto.UpdatePostRequest;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final CommunityService communityService;

    @Transactional
    public PostResponse createPost(Long communityId, CreatePostRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Community community = communityService.getCommunityOrThrow(communityId);
        communityService.requireApprovedMember(userId, communityId);

        Post post = Post.builder()
                .community(community)
                .authorId(userId)
                .postType(request.postType())
                .title(request.title())
                .content(request.content().trim())
                .imageUrl(request.imageUrl())
                .build();
        post = postRepository.save(post);
        return toResponse(post, 0L, false);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> listPosts(Long communityId, PostType postType, Pageable pageable) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);

        Specification<Post> spec = Specification.allOf(
                PostSpecifications.notDeleted(),
                PostSpecifications.inCommunity(communityId),
                PostSpecifications.hasType(postType)
        );
        Page<Post> page = postRepository.findAll(spec, pageable);
        List<Long> postIds = page.getContent().stream().map(Post::getId).toList();

        Map<Long, Long> likeCounts = postIds.isEmpty() ? Map.of() : postLikeRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(PostLikeRepository.PostLikeCount::getPostId, PostLikeRepository.PostLikeCount::getTotal));

        return PagedResponse.of(page, post -> toResponse(post, likeCounts.getOrDefault(post.getId(), 0L),
                postLikeRepository.existsByPostIdAndUserId(post.getId(), userId)));
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = getPostOrThrow(postId);
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, post.getCommunity().getId());
        long likeCount = postLikeRepository.countByPostId(postId);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, userId);
        return toResponse(post, likeCount, liked);
    }

    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request) {
        Post post = getPostOrThrow(postId);
        SecurityUtils.requireOwnerOrRole(post.getAuthorId(), Role.PLATFORM_ADMIN);

        post.setTitle(request.title());
        post.setContent(request.content().trim());
        post.setImageUrl(request.imageUrl());

        long likeCount = postLikeRepository.countByPostId(postId);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, SecurityUtils.currentUserId());
        return toResponse(post, likeCount, liked);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = getPostOrThrow(postId);
        Community community = post.getCommunity();
        Long userId = SecurityUtils.currentUserId();
        boolean isAuthor = post.getAuthorId().equals(userId);
        if (!isAuthor) {
            communityService.requireCommunityAdmin(community);
        }
        post.markDeleted(userId);
    }

    @Transactional
    public PostResponse setPinned(Long postId, boolean pinned) {
        Post post = getPostOrThrow(postId);
        communityService.requireCommunityAdmin(post.getCommunity());
        post.setPinned(pinned);
        long likeCount = postLikeRepository.countByPostId(postId);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, SecurityUtils.currentUserId());
        return toResponse(post, likeCount, liked);
    }

    @Transactional
    public void likePost(Long postId) {
        Post post = getPostOrThrow(postId);
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, post.getCommunity().getId());
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            return;
        }
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));
        postLikeRepository.save(PostLike.builder().post(post).user(user).build());
    }

    @Transactional
    public void unlikePost(Long postId) {
        Post post = getPostOrThrow(postId);
        Long userId = SecurityUtils.currentUserId();
        postLikeRepository.findByPostIdAndUserId(post.getId(), userId).ifPresent(postLikeRepository::delete);
    }

    @Transactional
    public PostCommentResponse addComment(Long postId, CreatePostCommentRequest request) {
        Post post = getPostOrThrow(postId);
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, post.getCommunity().getId());

        User author = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));
        PostComment comment = postCommentRepository.save(
                PostComment.builder().post(post).author(author).content(request.content().trim()).build());

        return new PostCommentResponse(comment.getId(), postId, author.getId(), author.getFullName(),
                comment.getContent(), comment.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostCommentResponse> listComments(Long postId, Pageable pageable) {
        Post post = getPostOrThrow(postId);
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), post.getCommunity().getId());

        Page<PostComment> page = postCommentRepository.findAllByPostIdOrderByCreatedAtAsc(postId, pageable);
        return PagedResponse.of(page, c -> new PostCommentResponse(
                c.getId(), postId, c.getAuthor().getId(), c.getAuthor().getFullName(), c.getContent(), c.getCreatedAt()));
    }

    private Post getPostOrThrow(Long postId) {
        return postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private PostResponse toResponse(Post post, long likeCount, boolean likedByCurrentUser) {
        String authorName = userRepository.findByIdAndDeletedFalse(post.getAuthorId())
                .map(User::getFullName)
                .orElse("Unknown resident");
        long commentCount = postCommentRepository.countByPostId(post.getId());

        return new PostResponse(
                post.getId(), post.getCommunity().getId(), post.getAuthorId(), authorName, post.getPostType(),
                post.getTitle(), post.getContent(), post.getImageUrl(), post.isPinned(), likeCount, commentCount,
                likedByCurrentUser, post.getCreatedAt(), post.getUpdatedAt());
    }
}
