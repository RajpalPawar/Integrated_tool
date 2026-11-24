package com.connectprojects.backend.knowledge;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge/articles")
public class KnowledgeController {
    private final KnowledgeService service;

    public KnowledgeController(KnowledgeService service) {
        this.service = service;
    }

    @GetMapping
    public List<KnowledgeArticleResponse> search(@RequestParam(required = false) String q,
                                                Authentication authentication) {
        return service.search(q, authentication);
    }

    @GetMapping("/{slug}")
    public KnowledgeArticleResponse get(@PathVariable String slug, Authentication authentication) {
        return service.get(slug, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeArticleResponse create(@Valid @RequestBody KnowledgeArticleRequest request,
                                           Authentication authentication) {
        return service.create(request, authentication);
    }

    @PutMapping("/{id}")
    public KnowledgeArticleResponse update(@PathVariable Long id, @Valid @RequestBody KnowledgeArticleRequest request,
                                           Authentication authentication) {
        return service.update(id, request, authentication);
    }
}
