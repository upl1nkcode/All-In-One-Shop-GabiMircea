-- AllInOne Shop Supabase Row Level Security Policies
-- Run this after 001_create_schema.sql
-- These policies control access at the database level for Supabase

-- Enable RLS on all tables
ALTER TABLE stores ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE brands ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_prices ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE search_history ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- PUBLIC READ POLICIES (anyone can read product catalog)
-- ============================================================

CREATE POLICY "Stores are publicly readable"
  ON stores FOR SELECT
  USING (true);

CREATE POLICY "Categories are publicly readable"
  ON categories FOR SELECT
  USING (true);

CREATE POLICY "Brands are publicly readable"
  ON brands FOR SELECT
  USING (true);

CREATE POLICY "Products are publicly readable"
  ON products FOR SELECT
  USING (true);

CREATE POLICY "Product prices are publicly readable"
  ON product_prices FOR SELECT
  USING (true);

-- ============================================================
-- USER TABLE POLICIES
-- ============================================================

-- Users can read their own profile
CREATE POLICY "Users can read own profile"
  ON users FOR SELECT
  USING (id = auth.uid());

-- Users can update their own profile
CREATE POLICY "Users can update own profile"
  ON users FOR UPDATE
  USING (id = auth.uid())
  WITH CHECK (id = auth.uid());

-- ============================================================
-- FAVORITES POLICIES (authenticated users manage their own)
-- ============================================================

CREATE POLICY "Users can read own favorites"
  ON favorites FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Users can add own favorites"
  ON favorites FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can remove own favorites"
  ON favorites FOR DELETE
  USING (user_id = auth.uid());

-- ============================================================
-- SEARCH HISTORY POLICIES
-- ============================================================

CREATE POLICY "Users can read own search history"
  ON search_history FOR SELECT
  USING (user_id = auth.uid() OR user_id IS NULL);

CREATE POLICY "Anyone can insert search history"
  ON search_history FOR INSERT
  WITH CHECK (true);

-- ============================================================
-- SERVICE ROLE BYPASS
-- The Spring Boot backend connects as the service role,
-- which bypasses RLS. These policies are for direct
-- Supabase client access if needed in the future.
-- ============================================================
