-- Garante as Roles essenciais
INSERT INTO
    roles (name, deleted, created_at)
VALUES
    ('ROLE_ADMIN', false, CURRENT_TIMESTAMP) ON CONFLICT (name) DO NOTHING;

INSERT INTO
    roles (name, deleted, created_at)
VALUES
    ('ROLE_CUSTOMER', false, CURRENT_TIMESTAMP) ON CONFLICT (name) DO NOTHING;

-- Opcional: Se já tiveres permissões no sistema, insere aqui também
-- INSERT INTO permissions (name, description, deleted, created_at) ...