-- ============================================================
-- AllInOne Shop — Seed Data
-- Run after 001_schema.sql
-- ============================================================

-- STORES
INSERT INTO stores (id, name, website, logo_url) VALUES
  ('a1000000-0000-0000-0000-000000000001', 'Zalando', 'https://www.zalando.com', NULL),
  ('a1000000-0000-0000-0000-000000000002', 'Nike', 'https://www.nike.com', NULL),
  ('a1000000-0000-0000-0000-000000000003', 'AboutYou', 'https://www.aboutyou.com', NULL),
  ('a1000000-0000-0000-0000-000000000004', 'ASOS', 'https://www.asos.com', NULL),
  ('a1000000-0000-0000-0000-000000000005', 'H&M', 'https://www.hm.com', NULL)
ON CONFLICT (id) DO NOTHING;

-- CATEGORIES
INSERT INTO categories (id, name, slug, parent_id) VALUES
  ('b1000000-0000-0000-0000-000000000001', 'Men', 'men', NULL),
  ('b1000000-0000-0000-0000-000000000002', 'Women', 'women', NULL),
  ('b1000000-0000-0000-0000-000000000003', 'Sneakers', 'sneakers', NULL),
  ('b1000000-0000-0000-0000-000000000004', 'Hoodies', 'hoodies', NULL),
  ('b1000000-0000-0000-0000-000000000005', 'Jackets', 'jackets', NULL),
  ('b1000000-0000-0000-0000-000000000006', 'Pants', 'pants', NULL),
  ('b1000000-0000-0000-0000-000000000007', 'T-Shirts', 't-shirts', NULL),
  ('b1000000-0000-0000-0000-000000000008', 'Jeans', 'jeans', NULL),
  ('b1000000-0000-0000-0000-000000000009', 'Coats', 'coats', NULL),
  ('b1000000-0000-0000-0000-000000000010', 'Shorts', 'shorts', NULL),
  ('b1000000-0000-0000-0000-000000000011', 'Shirts', 'shirts', NULL)
ON CONFLICT (name) DO NOTHING;

-- BRANDS
INSERT INTO brands (id, name, logo_url) VALUES
  ('c1000000-0000-0000-0000-000000000001', 'Nike', NULL),
  ('c1000000-0000-0000-0000-000000000002', 'Adidas', NULL),
  ('c1000000-0000-0000-0000-000000000003', 'Zara', NULL),
  ('c1000000-0000-0000-0000-000000000004', 'Carhartt', NULL),
  ('c1000000-0000-0000-0000-000000000005', 'Levi''s', NULL),
  ('c1000000-0000-0000-0000-000000000006', 'H&M', NULL),
  ('c1000000-0000-0000-0000-000000000007', 'Puma', NULL),
  ('c1000000-0000-0000-0000-000000000008', 'Under Armour', NULL)
ON CONFLICT (name) DO NOTHING;

-- ADMIN USER (password: admin123 — BCrypt hash)
INSERT INTO users (id, email, password_hash, first_name, last_name, role) VALUES
  ('f1000000-0000-0000-0000-000000000001', 'admin@allinone.com',
   '$2b$10$zWrDqZFTnYXAFoaAVtej5.iS9.kPGbtj/dnfrxY.f2/BLOdn/ec1e',
   'Admin', 'User', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- PRODUCTS
INSERT INTO products (id, name, description, brand_id, category_id, gender, image_url, sizes, colors) VALUES
  ('d1000000-0000-0000-0000-000000000001', 'Classic Black Hoodie',
   'Comfortable cotton blend hoodie with adjustable drawstring hood and kangaroo pocket.',
   'c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000004', 'UNISEX',
   'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&q=80',
   ARRAY['S','M','L','XL'], ARRAY['Black']),
  ('d1000000-0000-0000-0000-000000000002', 'Premium Leather Jacket',
   'Genuine leather jacket with asymmetric zip closure.',
   'c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000005', 'MEN',
   'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800&q=80',
   ARRAY['S','M','L','XL'], ARRAY['Black','Brown']),
  ('d1000000-0000-0000-0000-000000000003', 'Cargo Utility Pants',
   'Durable cargo pants with multiple pockets. Made from heavyweight cotton canvas.',
   'c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000006', 'MEN',
   'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800&q=80',
   ARRAY['S','M','L','XL','XXL'], ARRAY['Green','Brown','Black']),
  ('d1000000-0000-0000-0000-000000000004', 'Air Max 270 Sneakers',
   'Iconic Nike Air Max 270 with large Air unit in the heel.',
   'c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000003', 'UNISEX',
   'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
   ARRAY['39','40','41','42','43','44','45'], ARRAY['Red','Black','White']),
  ('d1000000-0000-0000-0000-000000000005', 'Oversized Graphic Tee',
   'Relaxed fit cotton t-shirt with bold graphic print.',
   'c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000007', 'UNISEX',
   'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80',
   ARRAY['XS','S','M','L','XL'], ARRAY['White','Black','Grey']),
  ('d1000000-0000-0000-0000-000000000006', 'Slim Fit Denim Jeans',
   'Classic 511 slim fit jeans in dark wash.',
   'c1000000-0000-0000-0000-000000000005', 'b1000000-0000-0000-0000-000000000008', 'MEN',
   'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800&q=80',
   ARRAY['28','30','32','34','36'], ARRAY['Blue','Black']),
  ('d1000000-0000-0000-0000-000000000007', 'Wool Blend Coat',
   'Elegant double-breasted coat in wool blend.',
   'c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000009', 'WOMEN',
   'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=800&q=80',
   ARRAY['S','M','L','XL'], ARRAY['Grey','Navy','Black']),
  ('d1000000-0000-0000-0000-000000000008', 'Running Shorts',
   'Lightweight running shorts with built-in briefs.',
   'c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000010', 'MEN',
   'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80',
   ARRAY['XS','S','M','L','XL'], ARRAY['Black','Navy','Grey']),
  ('d1000000-0000-0000-0000-000000000009', 'Classic White Sneakers',
   'Stan Smith classic white leather sneakers with green heel tab.',
   'c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000003', 'UNISEX',
   'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&q=80',
   ARRAY['39','40','41','42','43','44'], ARRAY['White','Green']),
  ('d1000000-0000-0000-0000-000000000010', 'Tech Fleece Joggers',
   'Premium tech fleece joggers with tapered fit.',
   'c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000006', 'MEN',
   'https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=800&q=80',
   ARRAY['S','M','L','XL'], ARRAY['Black','Grey']),
  ('d1000000-0000-0000-0000-000000000011', 'Lightweight Bomber Jacket',
   'Casual bomber jacket with ribbed cuffs and hem.',
   'c1000000-0000-0000-0000-000000000006', 'b1000000-0000-0000-0000-000000000005', 'UNISEX',
   'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80',
   ARRAY['XS','S','M','L','XL'], ARRAY['Black','Green','Navy']),
  ('d1000000-0000-0000-0000-000000000012', 'Checkered Flannel Shirt',
   'Heavy-duty flannel shirt in classic checkered pattern.',
   'c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000011', 'MEN',
   'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&q=80',
   ARRAY['S','M','L','XL','XXL'], ARRAY['Red','Blue','Green'])
ON CONFLICT (id) DO NOTHING;

-- PRODUCT PRICES
INSERT INTO product_prices (id, product_id, store_id, price, original_price, currency, product_url, in_stock) VALUES
  ('e1000000-0000-0000-0000-000000000001','d1000000-0000-0000-0000-000000000001','a1000000-0000-0000-0000-000000000001',54.00,65.00,'EUR','https://www.zalando.com/nike-hoodie',true),
  ('e1000000-0000-0000-0000-000000000002','d1000000-0000-0000-0000-000000000001','a1000000-0000-0000-0000-000000000002',63.00,65.00,'EUR','https://www.nike.com/hoodie-black',true),
  ('e1000000-0000-0000-0000-000000000003','d1000000-0000-0000-0000-000000000001','a1000000-0000-0000-0000-000000000003',58.00,NULL,'EUR','https://www.aboutyou.com/nike-hoodie',true),
  ('e1000000-0000-0000-0000-000000000004','d1000000-0000-0000-0000-000000000002','a1000000-0000-0000-0000-000000000001',189.00,220.00,'EUR','https://www.zalando.com/zara-leather',true),
  ('e1000000-0000-0000-0000-000000000005','d1000000-0000-0000-0000-000000000002','a1000000-0000-0000-0000-000000000004',195.00,NULL,'EUR','https://www.asos.com/zara-jacket',true),
  ('e1000000-0000-0000-0000-000000000006','d1000000-0000-0000-0000-000000000002','a1000000-0000-0000-0000-000000000003',192.00,220.00,'EUR','https://www.aboutyou.com/zara-leather',true),
  ('e1000000-0000-0000-0000-000000000007','d1000000-0000-0000-0000-000000000003','a1000000-0000-0000-0000-000000000001',79.00,NULL,'EUR','https://www.zalando.com/carhartt-cargo',true),
  ('e1000000-0000-0000-0000-000000000008','d1000000-0000-0000-0000-000000000003','a1000000-0000-0000-0000-000000000004',82.00,90.00,'EUR','https://www.asos.com/cargo-pants',true),
  ('e1000000-0000-0000-0000-000000000009','d1000000-0000-0000-0000-000000000003','a1000000-0000-0000-0000-000000000005',75.00,NULL,'EUR','https://www.hm.com/cargo-pants',true),
  ('e1000000-0000-0000-0000-000000000010','d1000000-0000-0000-0000-000000000004','a1000000-0000-0000-0000-000000000002',150.00,160.00,'EUR','https://www.nike.com/air-max-270',true),
  ('e1000000-0000-0000-0000-000000000011','d1000000-0000-0000-0000-000000000004','a1000000-0000-0000-0000-000000000001',145.00,160.00,'EUR','https://www.zalando.com/nike-airmax',true),
  ('e1000000-0000-0000-0000-000000000012','d1000000-0000-0000-0000-000000000004','a1000000-0000-0000-0000-000000000004',155.00,NULL,'EUR','https://www.asos.com/nike-270',true),
  ('e1000000-0000-0000-0000-000000000013','d1000000-0000-0000-0000-000000000005','a1000000-0000-0000-0000-000000000001',35.00,42.00,'EUR','https://www.zalando.com/adidas-tee',true),
  ('e1000000-0000-0000-0000-000000000014','d1000000-0000-0000-0000-000000000005','a1000000-0000-0000-0000-000000000004',38.00,NULL,'EUR','https://www.asos.com/adidas-shirt',true),
  ('e1000000-0000-0000-0000-000000000015','d1000000-0000-0000-0000-000000000005','a1000000-0000-0000-0000-000000000005',32.00,NULL,'EUR','https://www.hm.com/graphic-tee',true),
  ('e1000000-0000-0000-0000-000000000016','d1000000-0000-0000-0000-000000000006','a1000000-0000-0000-0000-000000000001',89.00,110.00,'EUR','https://www.zalando.com/levis-511',true),
  ('e1000000-0000-0000-0000-000000000017','d1000000-0000-0000-0000-000000000006','a1000000-0000-0000-0000-000000000004',92.00,NULL,'EUR','https://www.asos.com/levis-jeans',true),
  ('e1000000-0000-0000-0000-000000000018','d1000000-0000-0000-0000-000000000006','a1000000-0000-0000-0000-000000000003',88.00,110.00,'EUR','https://www.aboutyou.com/levis-denim',true),
  ('e1000000-0000-0000-0000-000000000019','d1000000-0000-0000-0000-000000000007','a1000000-0000-0000-0000-000000000001',129.00,150.00,'EUR','https://www.zalando.com/zara-coat',true),
  ('e1000000-0000-0000-0000-000000000020','d1000000-0000-0000-0000-000000000007','a1000000-0000-0000-0000-000000000003',135.00,NULL,'EUR','https://www.aboutyou.com/wool-coat',true),
  ('e1000000-0000-0000-0000-000000000021','d1000000-0000-0000-0000-000000000007','a1000000-0000-0000-0000-000000000004',132.00,150.00,'EUR','https://www.asos.com/zara-coat',true),
  ('e1000000-0000-0000-0000-000000000022','d1000000-0000-0000-0000-000000000008','a1000000-0000-0000-0000-000000000001',45.00,NULL,'EUR','https://www.zalando.com/adidas-shorts',true),
  ('e1000000-0000-0000-0000-000000000023','d1000000-0000-0000-0000-000000000008','a1000000-0000-0000-0000-000000000004',48.00,55.00,'EUR','https://www.asos.com/running-shorts',true),
  ('e1000000-0000-0000-0000-000000000024','d1000000-0000-0000-0000-000000000008','a1000000-0000-0000-0000-000000000005',42.00,NULL,'EUR','https://www.hm.com/sports-shorts',true),
  ('e1000000-0000-0000-0000-000000000025','d1000000-0000-0000-0000-000000000009','a1000000-0000-0000-0000-000000000001',95.00,110.00,'EUR','https://www.zalando.com/stan-smith',true),
  ('e1000000-0000-0000-0000-000000000026','d1000000-0000-0000-0000-000000000009','a1000000-0000-0000-0000-000000000004',98.00,NULL,'EUR','https://www.asos.com/adidas-white',true),
  ('e1000000-0000-0000-0000-000000000027','d1000000-0000-0000-0000-000000000009','a1000000-0000-0000-0000-000000000003',93.00,110.00,'EUR','https://www.aboutyou.com/stansmith',true),
  ('e1000000-0000-0000-0000-000000000028','d1000000-0000-0000-0000-000000000010','a1000000-0000-0000-0000-000000000002',89.00,100.00,'EUR','https://www.nike.com/tech-fleece',true),
  ('e1000000-0000-0000-0000-000000000029','d1000000-0000-0000-0000-000000000010','a1000000-0000-0000-0000-000000000001',85.00,100.00,'EUR','https://www.zalando.com/nike-joggers',true),
  ('e1000000-0000-0000-0000-000000000030','d1000000-0000-0000-0000-000000000010','a1000000-0000-0000-0000-000000000004',92.00,NULL,'EUR','https://www.asos.com/nike-fleece',true),
  ('e1000000-0000-0000-0000-000000000031','d1000000-0000-0000-0000-000000000011','a1000000-0000-0000-0000-000000000005',49.00,NULL,'EUR','https://www.hm.com/bomber-jacket',true),
  ('e1000000-0000-0000-0000-000000000032','d1000000-0000-0000-0000-000000000011','a1000000-0000-0000-0000-000000000001',52.00,60.00,'EUR','https://www.zalando.com/hm-bomber',true),
  ('e1000000-0000-0000-0000-000000000033','d1000000-0000-0000-0000-000000000011','a1000000-0000-0000-0000-000000000004',55.00,NULL,'EUR','https://www.asos.com/bomber',true),
  ('e1000000-0000-0000-0000-000000000034','d1000000-0000-0000-0000-000000000012','a1000000-0000-0000-0000-000000000001',69.00,NULL,'EUR','https://www.zalando.com/carhartt-flannel',true),
  ('e1000000-0000-0000-0000-000000000035','d1000000-0000-0000-0000-000000000012','a1000000-0000-0000-0000-000000000004',72.00,80.00,'EUR','https://www.asos.com/flannel-shirt',true),
  ('e1000000-0000-0000-0000-000000000036','d1000000-0000-0000-0000-000000000012','a1000000-0000-0000-0000-000000000003',68.00,NULL,'EUR','https://www.aboutyou.com/checkered',true)
ON CONFLICT (product_id, store_id) DO NOTHING;
