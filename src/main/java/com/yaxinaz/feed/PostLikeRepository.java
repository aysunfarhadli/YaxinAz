package com.yaxinaz.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    @Query("select l.post.id as postId, count(l) as total from PostLike l where l.post.id in :postIds group by l.post.id")
    List<PostLikeCount> countByPostIdIn(@Param("postIds") List<Long> postIds);

    interface PostLikeCount {
        Long getPostId();
        Long getTotal();
    }
}
