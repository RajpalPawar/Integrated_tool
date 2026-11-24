CREATE TABLE knowledge_articles (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(180) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'General',
    content TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    author_id BIGINT REFERENCES app_users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_articles_published_updated
    ON knowledge_articles(published, updated_at DESC);

INSERT INTO app_user_permissions (user_id, permission)
SELECT id, 'knowledge.view' FROM app_users
ON CONFLICT DO NOTHING;

INSERT INTO app_user_permissions (user_id, permission)
SELECT id, 'knowledge.manage' FROM app_users
WHERE role IN ('MANAGER', 'ADMIN')
ON CONFLICT DO NOTHING;
