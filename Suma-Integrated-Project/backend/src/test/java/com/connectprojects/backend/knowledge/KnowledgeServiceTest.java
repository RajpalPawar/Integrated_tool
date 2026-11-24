package com.connectprojects.backend.knowledge;

import com.connectprojects.backend.account.AppUser;
import com.connectprojects.backend.account.UserRole;
import com.connectprojects.backend.auth.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeServiceTest {
    @Test
    void regularReaderOnlySearchesPublishedArticles() {
        KnowledgeArticleRepository articles = mock(KnowledgeArticleRepository.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        AppUser reader = mock(AppUser.class);
        Authentication authentication = mock(Authentication.class);
        when(currentUser.require(authentication, "knowledge.view")).thenReturn(reader);
        when(reader.getRole()).thenReturn(UserRole.TEAM_MEMBER);
        when(reader.getPermissions()).thenReturn(List.of("knowledge.view"));
        when(articles.search("password", false)).thenReturn(List.of());

        KnowledgeService service = new KnowledgeService(articles, currentUser);

        assertTrue(service.search("password", authentication).isEmpty());
        verify(articles).search("password", false);
    }

    @Test
    void articleManagersCanSearchDrafts() {
        KnowledgeArticleRepository articles = mock(KnowledgeArticleRepository.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        AppUser manager = mock(AppUser.class);
        Authentication authentication = mock(Authentication.class);
        when(currentUser.require(authentication, "knowledge.view")).thenReturn(manager);
        when(manager.getRole()).thenReturn(UserRole.MANAGER);
        when(manager.getPermissions()).thenReturn(List.of("knowledge.view", "knowledge.manage"));
        when(articles.search("", true)).thenReturn(List.of());

        KnowledgeService service = new KnowledgeService(articles, currentUser);

        assertTrue(service.search(null, authentication).isEmpty());
        verify(articles).search("", true);
    }
}
