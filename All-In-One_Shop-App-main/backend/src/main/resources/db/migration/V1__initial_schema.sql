-- ═══════════════════════════════════════════════════════════════════
-- V1 — AllInOne Shop: Initial Schema (3NF Compliant)
-- ═══════════════════════════════════════════════════════════════════

-- ─── Brands ──────────────────────────────────────────────────────
CREATE TABLE brands (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    logo_url    VARCHAR(500),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ─── Categories ──────────────────────────────────────────────────
CREATE TABLE categories (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    parent_id   UUID,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- ─── Stores ──────────────────────────────────────────────────────
CREATE TABLE stores (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    website     VARCHAR(500),
    logo_url    VARCHAR(500),
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ─── Users ───────────────────────────────────────────────────────
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    avatar_url      VARCHAR(500),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ─── Products ────────────────────────────────────────────────────
CREATE TABLE products (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(500) NOT NULL,
    description         TEXT,
    image_url           VARCHAR(500),
    additional_images   TEXT,
    sizes               TEXT,
    colors              TEXT,
    gender              VARCHAR(20) DEFAULT 'UNISEX',
    is_active           BOOLEAN DEFAULT TRUE,
    brand_id            UUID,
    category_id         UUID,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- ─── Product Prices ─────────────────────────────────────────────
CREATE TABLE product_prices (
    id              UUID PRIMARY KEY,
    product_id      UUID NOT NULL,
    store_id        UUID NOT NULL,
    price           DECIMAL(12,2) NOT NULL,
    original_price  DECIMAL(12,2),
    currency        VARCHAR(10) DEFAULT 'EUR',
    product_url     VARCHAR(1000),
    in_stock        BOOLEAN DEFAULT TRUE,
    last_checked    TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_price_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
);

-- ─── Favorites ──────────────────────────────────────────────────
CREATE TABLE favorites (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    product_id  UUID NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_product UNIQUE (user_id, product_id)
);

-- ─── Search History ─────────────────────────────────────────────
CREATE TABLE search_history (
    id              UUID PRIMARY KEY,
    user_id         UUID,
    search_query    VARCHAR(500) NOT NULL,
    results_count   INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_search_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ─── Indexes for performance ────────────────────────────────────
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_product_prices_product ON product_prices(product_id);
CREATE INDEX idx_product_prices_store ON product_prices(store_id);
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_favorites_product ON favorites(product_id);
CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_query ON search_history(search_query);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_categories_slug ON categories(slug);
