-- 1. Sequence para o ID da revisão
CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 50;

-- 2. Tabela de Informações da Revisão
CREATE TABLE revinfo (
    rev INTEGER NOT NULL,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
);

-- 3. Tabelas de Auditoria (com todos os campos que faltavam)

CREATE TABLE products_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    name VARCHAR(255),
    slug VARCHAR(255),
    description TEXT,
    price NUMERIC(19, 2),
    discount_price NUMERIC(19, 2),
    image_url VARCHAR(255),
    image_prompt VARCHAR(255),
    rating DOUBLE PRECISION,
    max_installments INTEGER,
    category_id BIGINT,
    brand_id BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_products_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

CREATE TABLE product_variants_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    sku VARCHAR(255),
    stock_quantity INTEGER,
    price NUMERIC(19, 2),
    name VARCHAR(255),
    image_url VARCHAR(255),
    product_id BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_product_variants_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

-- Se tiver outras entidades auditadas, adicione aqui.