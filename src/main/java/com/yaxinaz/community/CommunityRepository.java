package com.yaxinaz.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByIdAndDeletedFalse(Long id);

    Page<Community> findAllByDeletedFalse(Pageable pageable);

    Page<Community> findAllByDeletedFalseAndCityIgnoreCase(String city, Pageable pageable);
}
