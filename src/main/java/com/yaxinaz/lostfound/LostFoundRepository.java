package com.yaxinaz.lostfound;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LostFoundRepository extends JpaRepository<LostFoundItem, Long>, JpaSpecificationExecutor<LostFoundItem> {

    Optional<LostFoundItem> findByIdAndCommunityIdAndDeletedFalse(Long id, Long communityId);

    @Query("select l from LostFoundItem l where l.community.id = :communityId and l.deleted = false "
            + "and (lower(l.title) like lower(concat('%', :q, '%')) or lower(coalesce(l.description, '')) like lower(concat('%', :q, '%')))")
    List<LostFoundItem> searchByKeyword(@Param("communityId") Long communityId, @Param("q") String query, Pageable pageable);
}
