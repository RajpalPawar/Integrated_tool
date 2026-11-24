package com.connectprojects.backend.knowledge;

import java.time.Instant;

public record KnowledgeArticleResponse(Long id, String slug, String title, String category, String content,
                                        boolean published, String author, Instant createdAt, Instant updatedAt) {
    static KnowledgeArticleResponse from(KnowledgeArticle article) {
        return new KnowledgeArticleResponse(article.getId(), article.getSlug(), article.getTitle(),
                article.getCategory(), article.getContent(), article.isPublished(),
                article.getAuthor() == null ? "" : article.getAuthor().getFullName(),
                article.getCreatedAt(), article.getUpdatedAt());
    }
}
