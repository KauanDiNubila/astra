package com.astra.tracking.category.adapter.out.persistence;

import com.astra.tracking.category.domain.Category;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserId(UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);
}
