package com.yaxinaz.provider;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long>, JpaSpecificationExecutor<ProviderProfile> {

    Optional<ProviderProfile> findByIdAndDeletedFalse(Long id);

    Optional<ProviderProfile> findByUserIdAndDeletedFalse(Long userId);

    boolean existsByUserIdAndDeletedFalse(Long userId);

    @Query("select p from ProviderProfile p where p.deleted = false "
            + "and (lower(p.businessName) like lower(concat('%', :q, '%')) or lower(coalesce(p.bio, '')) like lower(concat('%', :q, '%')))")
    List<ProviderProfile> searchByKeyword(@Param("q") String query, Pageable pageable);
}
