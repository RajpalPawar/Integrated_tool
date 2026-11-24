package com.connectprojects.backend.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeArticleRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 100) String category,
        @NotBlank @Size(max = 50000) String content,
        boolean published) {
}
