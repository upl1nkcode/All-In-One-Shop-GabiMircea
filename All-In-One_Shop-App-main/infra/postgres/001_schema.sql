-- ============================================================
-- AllInOne Shop — Production Database Schema
-- PostgreSQL 16 / Supabase
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- ENUM TYPES
-- ============================================================
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE gender_type AS ENUM ('MEN', 'WOMEN', 'UNISEX');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ============================================================
-- STORES
-- ============================================================
CREATE TABLE IF NOT EXISTS stores (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    website     VARCHAR(500) NOT NULL,
    logo_url    VARCHAR(500),
    is_active   BOOLEAN DEFAULT true,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- CATEGORIES  (self-referencing for subcategories)
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    parent_id   UUID REFERENCES categories(id) ON DELETE SET NULL,
    image_url   VARCHAR(500),
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- BRANDS
-- ============================================================
CREATE TABLE IF NOT EXISTS brands (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- PRODUCTS
-- ============================================================
CREATE TABLE IF NOT EXISTS products (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(500) NOT NULL,
    description         TEXT,
    brand_id            UUID REFERENCES brands(id) ON DELETE SET NULL,
    category_id         UUID REFERENCES categories(id) ON DELETE SET NULL,
    gender              gender_type DEFAULT 'UNISEX',
    image_url           VARCHAR(500),
    additional_images   TEXT[],
    sizes               TEXT[],
    colors              TEXT[],
    is_active           BOOLEAN DEFAULT true,
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- PRODUCT PRICES  (bridge: product ↔ store)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_prices (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    store_id        UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    price           DECIMAL(10, 2) NOT NULL,
    original_price  DECIMAL(10, 2),
    currency        VARCHAR(3) DEFAULT 'EUR',
    product_url     VARCHAR(1000) NOT NULL,
    in_stock        BOOLEAN DEFAULT true,
    last_checked    TIMESTAMPTZ DEFAULT NOW(),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(product_id, store_id)
);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    avatar_url      VARCHAR(500),
    role            user_role DEFAULT 'USER',
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- FAVORITES  (bridge: user ↔ product)
-- ============================================================
CREATE TABLE IF NOT EXISTS favorites (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, product_id)
);

-- ============================================================
-- SEARCH HISTORY
-- ============================================================
CREATE TABLE IF NOT EXISTS search_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    search_query    VARCHAR(500) NOT NULL,
    results_count   INTEGER DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- SCRAPE RUNS  (track scraper jobs)
-- ============================================================
CREATE TABLE IF NOT EXISTS scrape_runs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id        UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    status          VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, RUNNING, COMPLETED, FAILED
    products_found  INTEGER DEFAULT 0,
    products_updated INTEGER DEFAULT 0,
    error_message   TEXT,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_products_brand      ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_category   ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_gender     ON products(gender);
CREATE INDEX IF NOT EXISTS idx_products_active     ON products(is_active);
CREATE INDEX IF NOT EXISTS idx_products_name       ON products USING gin(to_tsvector('english', name));

CREATE INDEX IF NOT EXISTS idx_prices_product      ON product_prices(product_id);
CREATE INDEX IF NOT EXISTS idx_prices_store        ON product_prices(store_id);
CREATE INDEX IF NOT EXISTS idx_prices_price        ON product_prices(price);

CREATE INDEX IF NOT EXISTS idx_favorites_user      ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_product   ON favorites(product_id);

CREATE INDEX IF NOT EXISTS idx_search_user         ON search_history(user_id);
CREATE INDEX IF NOT EXISTS idx_search_created      ON search_history(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_scrape_store        ON scrape_runs(store_id);
CREATE INDEX IF NOT EXISTS idx_scrape_status       ON scrape_runs(status);

-- ============================================================
-- TRIGGER: auto-update updated_at on row change
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    CREATE TRIGGER trg_products_updated_at
        BEFORE UPDATE ON products
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_stores_updated_at
        BEFORE UPDATE ON stores
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_users_updated_at
        BEFORE UPDATE ON users
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
