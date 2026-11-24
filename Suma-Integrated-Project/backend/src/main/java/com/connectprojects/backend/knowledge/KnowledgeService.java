package com.connectprojects.backend.knowledge;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.auth.CurrentUserService;
import com.connectprojects.backend.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class KnowledgeService {
    private final KnowledgeArticleRepository articles;
    private final CurrentUserService currentUser;

    public KnowledgeService(KnowledgeArticleRepository articles, CurrentUserService currentUser) {
        this.articles = articles;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleResponse> search(String query, Authentication authentication) {
        AppUser user = currentUser.require(authentication, "knowledge.view");
        if (query != null && query.length() > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Search text must be 100 characters or fewer.");
        }
        boolean canManage = user.getRole() == com.connectprojects.backend.account.UserRole.SUPER_USER
                || user.getPermissions().contains("knowledge.manage");
        return articles.search(query == null ? "" : query.trim(), canManage).stream()
                .map(KnowledgeArticleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeArticleResponse get(String slug, Authentication authentication) {
        AppUser user = currentUser.require(authentication, "knowledge.view");
        KnowledgeArticle article = articles.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Knowledge article not found."));
        if (!article.isPublished() && !canManage(user)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Knowledge article not found.");
        }
        return KnowledgeArticleResponse.from(article);
    }

    @Transactional
    public KnowledgeArticleResponse create(KnowledgeArticleRequest request, Authentication authentication) {
        AppUser user = currentUser.require(authentication, "knowledge.manage");
        String title = request.title().trim();
        String slug = slugify(title);
        if (articles.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "An article with this title already exists.");
        }
        KnowledgeArticle article = new KnowledgeArticle(slug, title, category(request.category()),
                request.content().trim(), request.published(), user);
        return KnowledgeArticleResponse.from(articles.save(article));
    }

    @Transactional
    public KnowledgeArticleResponse update(Long id, KnowledgeArticleRequest request, Authentication authentication) {
        currentUser.require(authentication, "knowledge.manage");
        KnowledgeArticle article = articles.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Knowledge article not found."));
        article.update(request.title().trim(), category(request.category()), request.content().trim(), request.published());
        return KnowledgeArticleResponse.from(article);
    }

    private boolean canManage(AppUser user) {
        return user.getRole() == com.connectprojects.backend.account.UserRole.SUPER_USER
                || user.getPermissions().contains("knowledge.manage");
    }

    private static String category(String value) {
        return value == null || value.isBlank() ? "General" : value.trim();
    }

    private static String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "article" : normalized;
    }
}
