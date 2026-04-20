// Mock data for the AllInOne Shop metasearch engine

export interface Store {
  id: string;
  name: string;
  website: string;
}

export interface ProductPrice {
  storeId: string;
  price: number;
  currency: string;
  productUrl: string;
}

export interface Product {
  id: string;
  name: string;
  brand: string;
  category: string;
  description: string;
  imageUrl: string;
  prices: ProductPrice[];
}

export const stores: Store[] = [
  { id: '1', name: 'Zalando', website: 'zalando.com' },
  { id: '2', name: 'Nike', website: 'nike.com' },
  { id: '3', name: 'AboutYou', website: 'aboutyou.com' },
  { id: '4', name: 'ASOS', website: 'asos.com' },
  { id: '5', name: 'H&M', website: 'hm.com' },
];

export const products: Product[] = [
  {
    id: '1',
    name: 'Classic Black Hoodie',
    brand: 'Nike',
    category: 'Hoodies',
    description: 'Comfortable cotton blend hoodie with adjustable drawstring hood and kangaroo pocket. Perfect for casual wear and light workouts.',
    imageUrl: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&q=80',
    prices: [
      { storeId: '1', price: 54, currency: '€', productUrl: 'https://zalando.com/nike-hoodie' },
      { storeId: '2', price: 63, currency: '€', productUrl: 'https://nike.com/hoodie-black' },
      { storeId: '3', price: 58, currency: '€', productUrl: 'https://aboutyou.com/nike-hoodie' },
    ],
  },
  {
    id: '2',
    name: 'Premium Leather Jacket',
    brand: 'Zara',
    category: 'Jackets',
    description: 'Genuine leather jacket with asymmetric zip closure. Features multiple pockets and quilted shoulder detail.',
    imageUrl: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800&q=80',
    prices: [
      { storeId: '1', price: 189, currency: '€', productUrl: 'https://zalando.com/zara-leather' },
      { storeId: '4', price: 195, currency: '€', productUrl: 'https://asos.com/zara-jacket' },
      { storeId: '3', price: 192, currency: '€', productUrl: 'https://aboutyou.com/zara-leather' },
    ],
  },
  {
    id: '3',
    name: 'Cargo Utility Pants',
    brand: 'Carhartt',
    category: 'Pants',
    description: 'Durable cargo pants with multiple pockets. Made from heavyweight cotton canvas. Available in various colors.',
    imageUrl: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800&q=80',
    prices: [
      { storeId: '1', price: 79, currency: '€', productUrl: 'https://zalando.com/carhartt-cargo' },
      { storeId: '4', price: 82, currency: '€', productUrl: 'https://asos.com/cargo-pants' },
      { storeId: '5', price: 75, currency: '€', productUrl: 'https://hm.com/cargo-pants' },
    ],
  },
  {
    id: '4',
    name: 'Air Max 270 Sneakers',
    brand: 'Nike',
    category: 'Sneakers',
    description: 'Iconic Nike Air Max 270 with large Air unit in the heel. Breathable mesh upper with synthetic overlays.',
    imageUrl: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
    prices: [
      { storeId: '2', price: 150, currency: '€', productUrl: 'https://nike.com/air-max-270' },
      { storeId: '1', price: 145, currency: '€', productUrl: 'https://zalando.com/nike-airmax' },
      { storeId: '4', price: 155, currency: '€', productUrl: 'https://asos.com/nike-270' },
    ],
  },
  {
    id: '5',
    name: 'Oversized Graphic Tee',
    brand: 'Adidas',
    category: 'T-Shirts',
    description: 'Relaxed fit cotton t-shirt with bold graphic print. Features ribbed crew neck and dropped shoulders.',
    imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80',
    prices: [
      { storeId: '1', price: 35, currency: '€', productUrl: 'https://zalando.com/adidas-tee' },
      { storeId: '4', price: 38, currency: '€', productUrl: 'https://asos.com/adidas-shirt' },
      { storeId: '5', price: 32, currency: '€', productUrl: 'https://hm.com/graphic-tee' },
    ],
  },
  {
    id: '6',
    name: 'Slim Fit Denim Jeans',
    brand: "Levi's",
    category: 'Jeans',
    description: 'Classic 511 slim fit jeans in dark wash. Made from premium stretch denim with 5-pocket styling.',
    imageUrl: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800&q=80',
    prices: [
      { storeId: '1', price: 89, currency: '€', productUrl: 'https://zalando.com/levis-511' },
      { storeId: '4', price: 92, currency: '€', productUrl: 'https://asos.com/levis-jeans' },
      { storeId: '3', price: 88, currency: '€', productUrl: 'https://aboutyou.com/levis-denim' },
    ],
  },
  {
    id: '7',
    name: 'Wool Blend Coat',
    brand: 'Zara',
    category: 'Coats',
    description: 'Elegant double-breasted coat in wool blend. Features notch lapels, side pockets and full lining.',
    imageUrl: 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=800&q=80',
    prices: [
      { storeId: '1', price: 129, currency: '€', productUrl: 'https://zalando.com/zara-coat' },
      { storeId: '3', price: 135, currency: '€', productUrl: 'https://aboutyou.com/wool-coat' },
      { storeId: '4', price: 132, currency: '€', productUrl: 'https://asos.com/zara-coat' },
    ],
  },
  {
    id: '8',
    name: 'Running Shorts',
    brand: 'Adidas',
    category: 'Shorts',
    description: 'Lightweight running shorts with built-in briefs. Made from moisture-wicking fabric with reflective details.',
    imageUrl: 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=800&q=80',
    prices: [
      { storeId: '1', price: 45, currency: '€', productUrl: 'https://zalando.com/adidas-shorts' },
      { storeId: '4', price: 48, currency: '€', productUrl: 'https://asos.com/running-shorts' },
      { storeId: '5', price: 42, currency: '€', productUrl: 'https://hm.com/sports-shorts' },
    ],
  },
  {
    id: '9',
    name: 'Classic White Sneakers',
    brand: 'Adidas',
    category: 'Sneakers',
    description: 'Stan Smith classic white leather sneakers with green heel tab. Timeless design for everyday wear.',
    imageUrl: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&q=80',
    prices: [
      { storeId: '1', price: 95, currency: '€', productUrl: 'https://zalando.com/stan-smith' },
      { storeId: '4', price: 98, currency: '€', productUrl: 'https://asos.com/adidas-white' },
      { storeId: '3', price: 93, currency: '€', productUrl: 'https://aboutyou.com/stansmith' },
    ],
  },
  {
    id: '10',
    name: 'Tech Fleece Joggers',
    brand: 'Nike',
    category: 'Pants',
    description: 'Premium tech fleece joggers with tapered fit. Features zippered pockets and adjustable waistband.',
    imageUrl: 'https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=800&q=80',
    prices: [
      { storeId: '2', price: 89, currency: '€', productUrl: 'https://nike.com/tech-fleece' },
      { storeId: '1', price: 85, currency: '€', productUrl: 'https://zalando.com/nike-joggers' },
      { storeId: '4', price: 92, currency: '€', productUrl: 'https://asos.com/nike-fleece' },
    ],
  },
  {
    id: '11',
    name: 'Lightweight Bomber Jacket',
    brand: 'H&M',
    category: 'Jackets',
    description: 'Casual bomber jacket with ribbed cuffs and hem. Features front zip closure and side pockets.',
    imageUrl: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80',
    prices: [
      { storeId: '5', price: 49, currency: '€', productUrl: 'https://hm.com/bomber-jacket' },
      { storeId: '1', price: 52, currency: '€', productUrl: 'https://zalando.com/hm-bomber' },
      { storeId: '4', price: 55, currency: '€', productUrl: 'https://asos.com/bomber' },
    ],
  },
  {
    id: '12',
    name: 'Checkered Flannel Shirt',
    brand: 'Carhartt',
    category: 'Shirts',
    description: 'Heavy-duty flannel shirt in classic checkered pattern. Made from 100% cotton with button-down collar.',
    imageUrl: 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&q=80',
    prices: [
      { storeId: '1', price: 69, currency: '€', productUrl: 'https://zalando.com/carhartt-flannel' },
      { storeId: '4', price: 72, currency: '€', productUrl: 'https://asos.com/flannel-shirt' },
      { storeId: '3', price: 68, currency: '€', productUrl: 'https://aboutyou.com/checkered' },
    ],
  },
];

export const categories = [
  'All',
  'Men',
  'Women',
  'Sneakers',
  'Hoodies',
  'Jackets',
  'Pants',
  'T-Shirts',
  'Jeans',
  'Coats',
  'Shorts',
  'Shirts',
];

export const brands = [
  'Nike',
  'Adidas',
  'Zara',
  'Carhartt',
  "Levi's",
  'H&M',
  'Puma',
  'Under Armour',
];

export const sizes = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];

export const colors = [
  'Black',
  'White',
  'Grey',
  'Navy',
  'Red',
  'Blue',
  'Green',
  'Brown',
];
