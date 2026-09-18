package com.yaxinaz.feed;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Optional<Post> findByIdAndDeletedFalse(Long id);

    long countByCommunityIdAndDeletedFalse(Long communityId);

    @Query("select p from Post p where p.community.id = :communityId and p.deleted = false "
            + "and (lower(coalesce(p.title, '')) like lower(concat('%', :q, '%')) or lower(p.content) like lower(concat('%', :q, '%')))")
    List<Post> searchByKeyword(@Param("communityId") Long communityId, @Param("q") String query, Pageable pageable);
}
