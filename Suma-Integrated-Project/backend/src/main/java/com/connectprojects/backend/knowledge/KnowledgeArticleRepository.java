package com.connectprojects.backend.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    boolean existsBySlug(String slug);
    Optional<KnowledgeArticle> findBySlug(String slug);

    @Query("""
            select a from KnowledgeArticle a
            where (:canManage = true or a.published = true)
              and (:query = '' or lower(a.title) like lower(concat('%', :query, '%'))
                   or lower(a.category) like lower(concat('%', :query, '%'))
                   or lower(a.content) like lower(concat('%', :query, '%')))
            order by a.updatedAt desc
            """)
    List<KnowledgeArticle> search(@Param("query") String query, @Param("canManage") boolean canManage);
}
