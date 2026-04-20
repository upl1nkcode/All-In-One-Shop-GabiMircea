# All-In-One Shop App - Complete Architecture Guide

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture Overview](#architecture-overview)
4. [Frontend Architecture](#frontend-architecture)
5. [Backend Architecture](#backend-architecture)
6. [Database Architecture](#database-architecture)
7. [Authentication & Security](#authentication--security)
8. [How Frontend & Backend Communicate](#how-frontend--backend-communicate)
9. [File Structure Explained](#file-structure-explained)
10. [Key Concepts Explained](#key-concepts-explained)
11. [Data Flow Examples](#data-flow-examples)

---

## 🎯 Project Overview

**All-In-One Shop** is a meta search engine for fashion products. It aggregates products from multiple online stores, allowing users to:
- Search and browse fashion products across different retailers
- Compare prices from different stores
- Save favorite products
- View product details (images, sizes, colors, prices)

Think of it like "Google for fashion shopping" - one search shows results from many stores.

---

## 🛠 Technology Stack

### Frontend
- **React 18** - UI library for building interactive interfaces
- **TypeScript** - Type-safe JavaScript
- **Vite** - Fast build tool and development server
- **React Router** - Client-side routing/navigation
- **SWR** - Data fetching and caching library
- **Radix UI** - Headless UI components (accessible, customizable)
- **Material-UI (MUI)** - Pre-built React components
- **Tailwind CSS** - Utility-first CSS framework

### Backend
- **Java 17** (upgrading to 21)
- **Spring Boot 3.2.3** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database interaction
- **JWT (JSON Web Tokens)** - Token-based authentication
- **PostgreSQL** - Relational database
- **Hibernate** - ORM (Object-Relational Mapping)
- **Maven** - Build tool
- **SpringDoc OpenAPI** - API documentation (Swagger)

### Database
- **Supabase** - PostgreSQL database hosting (cloud-based)
- **PostgreSQL** - Powerful open-source relational database

---

## 🏗 Architecture Overview

This is a **3-tier web application** architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION TIER                         │
│  (Frontend - React/TypeScript running in browser)           │
│                                                              │
│  - User Interface (UI Components)                           │
│  - User Interactions                                        │
│  - Client-side Routing                                      │
│  - State Management                                         │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   │ HTTP/REST API
                   │ (JSON over HTTPS)
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                    APPLICATION TIER                          │
│  (Backend - Spring Boot running on server)                  │
│                                                              │
│  - REST API Endpoints                                       │
│  - Business Logic                                           │
│  - Authentication/Authorization                             │
│  - Data Validation                                          │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   │ JDBC/JPA
                   │ (SQL queries)
                   │
┌──────────────────▼──────────────────────────────────────────┐
│                    DATA TIER                                 │
│  (Supabase PostgreSQL Database)                             │
│                                                              │
│  - User Data                                                │
│  - Product Information                                      │
│  - Store/Brand/Category Data                               │
│  - Favorites                                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎨 Frontend Architecture

### Overview
The frontend is a **Single Page Application (SPA)** built with React. It runs entirely in the user's browser and communicates with the backend through REST API calls.

### Key Directories & Files

```
src/
├── main.tsx                    # Application entry point
├── app/
│   ├── App.tsx                 # Root component with providers
│   ├── routes.ts               # Route definitions
│   ├── api/
│   │   ├── client.ts           # API communication functions
│   │   ├── hooks.ts            # Custom React hooks for data fetching
│   │   └── types.ts            # TypeScript type definitions
│   ├── components/
│   │   ├── LandingPage.tsx     # Home page
│   │   ├── ProductCard.tsx     # Product display card
│   │   ├── ProductDetail.tsx   # Product detail page
│   │   ├── AuthModal.tsx       # Login/Register modal
│   │   ├── UserMenu.tsx        # User profile dropdown
│   │   └── ui/                 # Reusable UI components
│   ├── context/
│   │   └── AuthContext.tsx     # Authentication state management
│   └── data/
│       └── mockData.ts         # Sample data for development
└── styles/
    ├── index.css               # Global styles
    └── tailwind.css            # Tailwind utilities
```

### How It Works

#### 1. **Application Entry Point** (`main.tsx`)
```typescript
createRoot(document.getElementById("root")!).render(<App />);
```
- This is where React starts
- Finds the HTML element with id="root" 
- Renders the entire app inside it

#### 2. **Root Component** (`App.tsx`)
```typescript
<SWRConfig>          {/* Configures data fetching */}
  <AuthProvider>     {/* Provides authentication state */}
    <RouterProvider router={router} />  {/* Handles navigation */}
    <Toaster />      {/* Shows notifications */}
  </AuthProvider>
</SWRConfig>
```

**What are Providers?**
- Think of them like "wrappers" that give special abilities to all child components
- `AuthProvider`: Makes current user info available everywhere
- `RouterProvider`: Enables page navigation without full page reload
- `SWRConfig`: Manages API data fetching and caching

#### 3. **Routing** (`routes.ts`)
Defines which component shows for each URL:
```
/ (root)                    → LandingPage
/product/:id                → ProductDetail
/favorites                  → FavoritesPage
/search?query=shoes         → SearchResults
```

#### 4. **API Client** (`api/client.ts`)
Handles all backend communication:

```typescript
// Example: Login request
async function login(email: string, password: string) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  return response.json();
}
```

**Key Functions:**
- `login()` - Authenticate user
- `register()` - Create new account
- `searchProducts()` - Search for products
- `getFavorites()` - Get user's favorites
- `addFavorite()` - Add product to favorites

#### 5. **Custom Hooks** (`api/hooks.ts`)
React hooks that make data fetching easier:

```typescript
// Instead of fetching manually in every component:
const useProducts = () => {
  const { data, error, isLoading } = useSWR('/products', fetchProducts);
  return { products: data, error, isLoading };
};

// Use in component:
function ProductList() {
  const { products, isLoading } = useProducts();
  
  if (isLoading) return <div>Loading...</div>;
  return <div>{products.map(p => <ProductCard product={p} />)}</div>;
}
```

**Benefits:**
- Automatic caching (no redundant API calls)
- Automatic revalidation
- Loading/error states handled automatically

#### 6. **Authentication Context** (`context/AuthContext.tsx`)
Manages user authentication state across the entire app:

```typescript
const AuthContext = {
  user: null | User,           // Current logged-in user
  isLoading: boolean,          // Is checking authentication?
  login: (email, pass) => {},  // Login function
  logout: () => {},            // Logout function
  register: (data) => {}       // Register function
}
```

Any component can access this:
```typescript
const { user, login } = useAuth();

if (user) {
  return <div>Welcome, {user.name}!</div>;
}
```

---

## ⚙️ Backend Architecture

### Overview
The backend is built with **Spring Boot**, following the **MVC (Model-View-Controller)** pattern (though we use it as **MRC** - Model-Repository-Controller since we return JSON, not views).

### Architecture Pattern: Layered Architecture

```
┌─────────────────────────────────────────────────┐
│          CONTROLLER LAYER                       │
│  - Receives HTTP requests                       │
│  - Validates input                              │
│  - Returns HTTP responses                       │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│          SERVICE LAYER                          │
│  - Business logic                               │
│  - Transaction management                       │
│  - Orchestrates operations                      │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│          REPOSITORY LAYER                       │
│  - Database access                              │
│  - CRUD operations                              │
│  - Custom queries                               │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│          DATABASE (PostgreSQL)                  │
│  - Actual data storage                          │
└─────────────────────────────────────────────────┘
```

### File Structure

```
backend/src/main/java/com/allinoneshop/
├── AllInOneShopApplication.java    # Main entry point
├── controller/                     # REST API endpoints
│   ├── AuthController.java         # /api/auth/* (login, register)
│   ├── ProductController.java      # /api/products/*
│   ├── FavoriteController.java     # /api/favorites/*
│   ├── BrandController.java        # /api/brands/*
│   ├── CategoryController.java     # /api/categories/*
│   └── StoreController.java        # /api/stores/*
├── service/                        # Business logic
│   ├── AuthService.java
│   ├── ProductService.java
│   └── FavoriteService.java
├── repository/                     # Database access
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   └── FavoriteRepository.java
├── entity/                         # Database models (tables)
│   ├── User.java
│   ├── Product.java
│   ├── Brand.java
│   ├── Category.java
│   ├── Store.java
│   ├── ProductPrice.java
│   └── Favorite.java
├── security/                       # Authentication
│   ├── JwtTokenProvider.java      # Creates/validates JWT tokens
│   └── JwtAuthenticationFilter.java # Checks auth on requests
├── dto/                            # Data Transfer Objects
│   └── auth/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── AuthResponse.java
├── exception/                      # Error handling
└── config/                         # Configuration
```

### Layer Responsibilities

#### 1. **Entity Layer** (Models)
Represents database tables as Java classes.

**Example: User.java**
```java
@Entity                                    // This is a database table
@Table(name = "users")                     // Table name in DB
public class User {
    
    @Id                                    // Primary key
    @GeneratedValue(strategy = IDENTITY)   // Auto-increment ID
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    private String firstName;
    private String lastName;
    
    @OneToMany(mappedBy = "user")          // Relationship: One user has many favorites
    private List<Favorite> favorites;
}
```

**JPA/Hibernate does the magic:**
- Automatically creates SQL queries
- Maps Java objects ↔ Database rows
- Handles relationships between tables

#### 2. **Repository Layer**
Database access interface. Spring Data JPA auto-implements most methods.

**Example: UserRepository.java**
```java
public interface UserRepository extends JpaRepository<User, UUID> {
    // Spring automatically implements these based on method names:
    Optional<User> findByEmail(String email);          // SELECT * FROM users WHERE email = ?
    boolean existsByEmail(String email);               // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    List<User> findByLastName(String lastName);        // SELECT * FROM users WHERE last_name = ?
    
    // Custom query
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    List<User> findRecentUsers(LocalDateTime date);
}
```

**You don't write SQL!** Spring generates it automatically.

#### 3. **Service Layer**
Business logic and orchestration.

**Example: ProductService.java**
```java
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private BrandRepository brandRepository;
    
    public List<Product> searchProducts(String query) {
        // Business logic: Search in name and description
        return productRepository.findByNameContainingOrDescriptionContaining(
            query, query
        );
    }
    
    public Product createProduct(ProductDTO dto) {
        // Validation
        if (dto.getName() == null) {
            throw new ValidationException("Product name required");
        }
        
        // Business logic
        Brand brand = brandRepository.findById(dto.getBrandId())
            .orElseThrow(() -> new NotFoundException("Brand not found"));
        
        Product product = new Product();
        product.setName(dto.getName());
        product.setBrand(brand);
        
        return productRepository.save(product);
    }
}
```

#### 4. **Controller Layer**
REST API endpoints. Receives HTTP requests, calls services, returns responses.

**Example: ProductController.java**
```java
@RestController                                  // Indicates this handles HTTP requests
@RequestMapping("/api/products")                 // Base path for all endpoints
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // GET /api/products/search?query=shoes
    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(
        @RequestParam String query                // Gets value from URL parameter
    ) {
        List<Product> products = productService.searchProducts(query);
        return ResponseEntity.ok(products);       // Returns 200 OK with JSON data
    }
    
    // GET /api/products/123e4567-e89b-12d3-a456-426614174000
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(
        @PathVariable UUID id                     // Gets value from URL path
    ) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    // POST /api/products
    @PostMapping
    public ResponseEntity<Product> createProduct(
        @RequestBody ProductDTO dto               // Gets data from request body (JSON)
    ) {
        Product created = productService.createProduct(dto);
        return ResponseEntity.status(201).body(created);  // Returns 201 CREATED
    }
}
```

**HTTP Methods:**
- `GET` - Retrieve data (read)
- `POST` - Create new data
- `PUT` - Update existing data
- `DELETE` - Remove data

---

## 🗄️ Database Architecture

### Supabase Connection

**What is Supabase?**
- Cloud-hosted PostgreSQL database
- Provides a PostgreSQL database with authentication, storage, and APIs
- Alternative to Firebase (but uses PostgreSQL instead of NoSQL)

**How Your App Connects:**

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://db.supabase.com:5432/postgres
    username: postgres.yourproject
    password: your-password
    driver-class-name: org.postgresql.Driver
```

**Connection Flow:**
```
Spring Boot App
     │
     │ JDBC Driver (PostgreSQL)
     │
     ▼
Supabase PostgreSQL Database
     │
     │ Stores actual data on disk
     │
     ▼
Tables: users, products, brands, categories, stores, etc.
```

### Database Schema

**Main Tables:**

```sql
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   brands    │         │ categories  │         │   stores    │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │         │ id (PK)     │
│ name        │         │ name        │         │ name        │
│ logo_url    │         │ slug        │         │ website     │
└──────┬──────┘         │ parent_id   │         │ logo_url    │
       │                └──────┬──────┘         └──────┬──────┘
       │                       │                        │
       │                       │                        │
       │                ┌──────▼────────────────────────▼───────┐
       │                │           products                     │
       └────────────────┤                                        │
                        ├────────────────────────────────────────┤
                        │ id (PK)                                │
                        │ name                                   │
                        │ description                            │
                        │ brand_id (FK) ──────────────▶ brands   │
                        │ category_id (FK) ────────▶ categories  │
                        │ image_url                              │
                        │ additional_images (array)              │
                        │ sizes (array)                          │
                        │ colors (array)                         │
                        └───────┬──────────────┬─────────────────┘
                                │              │
                                │              │
                    ┌───────────▼─────┐   ┌────▼─────────┐
                    │ product_prices  │   │  favorites   │
                    ├─────────────────┤   ├──────────────┤
                    │ id (PK)         │   │ id (PK)      │
                    │ product_id (FK) │   │ user_id (FK) │
                    │ store_id (FK)   │   │ product_id   │
                    │ price           │   └──────────────┘
                    │ currency        │
                    │ product_url     │
                    └─────────────────┘

                    ┌─────────────┐
                    │    users    │
                    ├─────────────┤
                    │ id (PK)     │
                    │ email       │
                    │ password    │
                    │ first_name  │
                    │ last_name   │
                    └──────┬──────┘
                           │
                           └──┐
                              │
                    ┌─────────▼──────┐
                    │   favorites    │
                    └────────────────┘
```

**Relationships:**
- **One-to-Many**: One brand has many products
- **One-to-Many**: One category has many products
- **One-to-Many**: One product has many prices (different stores)
- **Many-to-Many**: Users ↔ Products (through favorites table)

---

## 🔐 Authentication & Security

### JWT-Based Authentication

**What is JWT?**
JSON Web Token - a secure way to transmit information between parties as a JSON object.

**Structure:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

[Header].[Payload].[Signature]
```

**Contains:**
- User ID
- Email
- Issued date
- Expiration date (24 hours)

### Authentication Flow

```
┌─────────────┐                                    ┌──────────────┐
│   Browser   │                                    │   Backend    │
│             │                                    │              │
│ 1. User     │    POST /api/auth/login           │              │
│    enters   │    { email, password }             │              │
│    email &  ├───────────────────────────────────▶│              │
│    password │                                    │ 2. Verify    │
│             │                                    │    password  │
│             │                                    │    with DB   │
│             │                                    │              │
│ 4. Store    │    200 OK                         │ 3. Generate  │
│    token in │    { token: "eyJ...", user: {...}}│    JWT token │
│    localStorage◀───────────────────────────────┤              │
│             │                                    │              │
│ 5. Include  │    GET /api/favorites             │              │
│    token in │    Header: "Authorization: Bearer │              │
│    future   │            eyJ..."                 │              │
│    requests ├───────────────────────────────────▶│ 6. Verify   │
│             │                                    │    token     │
│             │                                    │              │
│             │    200 OK                         │ 7. Return    │
│             │    [{ id: 1, ...}, ...]           │    data      │
│             │◀───────────────────────────────────┤              │
└─────────────┘                                    └──────────────┘
```

### Security Features

**1. Password Hashing**
```java
// Passwords NEVER stored in plain text!
String plainPassword = "mypassword123";
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
// Stored: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**2. JWT Token Validation**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        // 1. Extract token from "Authorization: Bearer {token}" header
        String token = getTokenFromRequest(request);
        
        // 2. Validate token (not expired, not tampered)
        if (jwtTokenProvider.validateToken(token)) {
            // 3. Extract user info from token
            String email = jwtTokenProvider.getEmailFromToken(token);
            
            // 4. Load user details
            UserDetails user = userDetailsService.loadUserByUsername(email);
            
            // 5. Set authentication in context (user is now authenticated)
            SecurityContextHolder.getContext().setAuthentication(...);
        }
    }
}
```

**3. Endpoint Protection**
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()      // Anyone can access
                .requestMatchers("/api/products/**").permitAll()  // Public endpoints
                .requestMatchers("/api/favorites/**").authenticated() // Must be logged in
                .anyRequest().authenticated()                      // Default: require auth
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 🔄 How Frontend & Backend Communicate

### REST API Communication

**1. Frontend Makes Request**

```typescript
// In React component
const handleLogin = async () => {
  try {
    // 1. Call API client function
    const response = await apiClient.login(email, password);
    
    // 2. Store token
    setAuthToken(response.token);
    
    // 3. Update UI
    setUser(response.user);
  } catch (error) {
    console.error('Login failed:', error);
  }
};
```

**2. API Client Sends HTTP Request**

```typescript
// api/client.ts
async function login(email: string, password: string) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',  // Tell server we're sending JSON
    },
    body: JSON.stringify({ email, password })  // Convert JS object to JSON string
  });
  
  if (!response.ok) {
    throw new Error('Login failed');
  }
  
  return response.json();  // Parse JSON response to JS object
}
```

**3. Request Travels Over Network**

```
Frontend (localhost:5173)
     │
     │ HTTP POST to http://localhost:8080/api/auth/login
     │
     │ Headers:
     │   Content-Type: application/json
     │
     │ Body:
     │   { "email": "user@example.com", "password": "secret123" }
     │
     ▼
Backend (localhost:8080)
```

**4. Backend Receives & Processes**

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Spring automatically converts JSON to LoginRequest object
        String email = request.getEmail();
        String password = request.getPassword();
        
        // 2. Validate credentials
        User user = authService.authenticate(email, password);
        
        // 3. Generate JWT token
        String token = jwtTokenProvider.generateToken(user);
        
        // 4. Create response
        AuthResponse response = new AuthResponse(token, user);
        
        // 5. Spring automatically converts response object to JSON
        return ResponseEntity.ok(response);
    }
}
```

**5. Response Travels Back**

```
Backend (localhost:8080)
     │
     │ HTTP 200 OK
     │
     │ Headers:
     │   Content-Type: application/json
     │
     │ Body:
     │   {
     │     "token": "eyJhbGciOiJIUzI1NiIs...",
     │     "user": {
     │       "id": "123e4567-e89b-12d3-a456-426614174000",
     │       "email": "user@example.com",
     │       "firstName": "John",
     │       "lastName": "Doe"
     │     }
     │   }
     │
     ▼
Frontend (localhost:5173)
```

**6. Frontend Receives & Updates UI**

```typescript
// Response is automatically parsed to JavaScript object
const data = {
  token: "eyJhbGciOiJIUzI1NiIs...",
  user: {
    id: "123e4567-e89b-12d3-a456-426614174000",
    email: "user@example.com",
    firstName: "John",
    lastName: "Doe"
  }
};

// Store token for future requests
localStorage.setItem('auth_token', data.token);

// Update React state → UI re-renders
setUser(data.user);
```

### CORS (Cross-Origin Resource Sharing)

**Problem:** Browser blocks requests from `localhost:5173` (frontend) to `localhost:8080` (backend) by default for security.

**Solution:** Backend explicitly allows frontend origin:

```yaml
# application.yml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000
```

```java
// Spring Security config
@Configuration
public class SecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        return source;
    }
}
```

---

## 📁 File Structure Explained

### Frontend Files

| File | Purpose |
|------|---------|
| `main.tsx` | React entry point - where app starts |
| `App.tsx` | Root component with providers (Auth, Router, SWR) |
| `routes.ts` | Defines URL → Component mappings |
| `api/client.ts` | Functions to call backend API |
| `api/hooks.ts` | React hooks for data fetching |
| `api/types.ts` | TypeScript interfaces for API data |
| `context/AuthContext.tsx` | Global authentication state |
| `components/LandingPage.tsx` | Home page with search |
| `components/ProductCard.tsx` | Product display card (reusable) |
| `components/ProductDetail.tsx` | Product details page |
| `components/AuthModal.tsx` | Login/register popup |
| `components/ui/*` | Reusable UI components (buttons, inputs, etc.) |

### Backend Files

| File | Purpose |
|------|---------|
| `AllInOneShopApplication.java` | Spring Boot entry point (`main` method) |
| `controller/AuthController.java` | `/api/auth/*` endpoints (login, register) |
| `controller/ProductController.java` | `/api/products/*` endpoints |
| `controller/FavoriteController.java` | `/api/favorites/*` endpoints |
| `service/AuthService.java` | Authentication business logic |
| `service/ProductService.java` | Product business logic |
| `repository/UserRepository.java` | Database access for users table |
| `repository/ProductRepository.java` | Database access for products table |
| `entity/User.java` | User database model/table |
| `entity/Product.java` | Product database model/table |
| `entity/Brand.java` | Brand database model/table |
| `entity/Favorite.java` | Favorite database model/table |
| `security/JwtTokenProvider.java` | Creates & validates JWT tokens |
| `security/JwtAuthenticationFilter.java` | Intercepts requests to check auth |
| `dto/auth/LoginRequest.java` | Structure for login request data |
| `dto/auth/AuthResponse.java` | Structure for login response data |
| `application.yml` | Configuration (database, JWT, CORS, etc.) |
| `pom.xml` | Maven dependencies & build config |

---

## 💡 Key Concepts Explained

### What is Spring Boot?

**Spring Boot** is a framework that makes building Java web applications easier.

**Without Spring Boot:**
```java
// You'd have to manually:
- Set up a web server (Tomcat)
- Configure database connections
- Write SQL queries
- Handle HTTP requests/responses
- Manage application lifecycle
- Deal with tons of configuration files
```

**With Spring Boot:**
```java
@SpringBootApplication  // This annotation does ALL the setup!
public class AllInOneShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllInOneShopApplication.class, args);
    }
}

@RestController  // This class handles HTTP requests
@RequestMapping("/api/products")
public class ProductController {
    @GetMapping  // This method handles GET requests
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }
}
```

**Spring Boot Features:**
- **Auto-configuration**: Automatically configures your application
- **Embedded server**: Built-in Tomcat (no separate server needed)
- **Starter dependencies**: Pre-packaged dependency sets
- **Production-ready**: Health checks, metrics, monitoring built-in

### Dependency Injection (DI)

**Problem:** Objects need other objects to work. Creating them manually is messy:

```java
// Bad: Manual creation
public class ProductController {
    private ProductService productService = new ProductService();
    // But ProductService needs ProductRepository...
    // Which needs DatabaseConnection...
    // This gets complicated fast!
}
```

**Solution:** Spring creates and "injects" dependencies automatically:

```java
// Good: Spring creates and injects
@RestController
public class ProductController {
    
    @Autowired  // Spring automatically provides this
    private ProductService productService;
    
    // Spring manages the entire dependency tree!
}
```

**How it works:**
1. Spring scans for `@Component`, `@Service`, `@Repository`, `@Controller`
2. Creates instances of these classes
3. Injects them where needed (`@Autowired`)
4. Manages their lifecycle

### ORM (Object-Relational Mapping)

**Problem:** Databases use tables and SQL. Java uses objects. How to connect them?

**Without ORM (manual):**
```java
// Execute SQL manually
String sql = "SELECT * FROM users WHERE email = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, email);
ResultSet rs = stmt.executeQuery();

// Manually convert ResultSet to User object
User user = new User();
user.setId(rs.getString("id"));
user.setEmail(rs.getString("email"));
user.setFirstName(rs.getString("first_name"));
// ... etc
```

**With ORM (JPA/Hibernate):**
```java
// Just call a method!
User user = userRepository.findByEmail(email);

// Hibernate automatically:
// - Generates SQL query
// - Executes it
// - Converts results to User object
```

**Benefits:**
- Write less code
- No SQL (mostly)
- Database-independent (can switch from PostgreSQL to MySQL easily)
- Handles relationships automatically

### JPA Annotations Explained

```java
@Entity  // This class represents a database table
@Table(name = "users")  // Table is named "users"
public class User {
    
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.UUID)  // Auto-generate UUID
    private UUID id;
    
    @Column(nullable = false, unique = true)  // Can't be null, must be unique
    private String email;
    
    @Column(name = "password_hash")  // Column name in DB is "password_hash"
    private String passwordHash;
    
    @OneToMany(mappedBy = "user")  // One user has many favorites
    private List<Favorite> favorites;
    
    @ManyToOne  // Many products belong to one brand
    @JoinColumn(name = "brand_id")
    private Brand brand;
}
```

### React Hooks Explained

**Hooks** are functions that let you use React features in function components.

**useState** - Manage component state:
```typescript
const [count, setCount] = useState(0);  // Initial value: 0

<button onClick={() => setCount(count + 1)}>
  Clicked {count} times
</button>
```

**useEffect** - Run code when component mounts or state changes:
```typescript
useEffect(() => {
  // Runs after component renders
  fetchProducts();
}, []);  // Empty array = run once on mount

useEffect(() => {
  // Runs when searchQuery changes
  fetchProducts(searchQuery);
}, [searchQuery]);  // Re-run when searchQuery changes
```

**Custom Hooks** - Reusable logic:
```typescript
function useProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    fetchProducts().then(data => {
      setProducts(data);
      setLoading(false);
    });
  }, []);
  
  return { products, loading };
}

// Use in component:
function ProductList() {
  const { products, loading } = useProducts();
  // ...
}
```

---

## 🔄 Data Flow Examples

### Example 1: User Logs In

**Step-by-step flow:**

1. **User enters email and password** → triggers form submission
   
2. **Frontend** (`AuthModal.tsx`):
   ```typescript
   const handleLogin = async (email, password) => {
     const response = await login(email, password);
     setAuthToken(response.token);
     setUser(response.user);
   };
   ```

3. **API Client** (`api/client.ts`):
   ```typescript
   async function login(email, password) {
     return fetch('http://localhost:8080/api/auth/login', {
       method: 'POST',
       body: JSON.stringify({ email, password })
     }).then(res => res.json());
   }
   ```

4. **Backend Controller** (`AuthController.java`):
   ```java
   @PostMapping("/login")
   public AuthResponse login(@RequestBody LoginRequest request) {
     return authService.authenticate(request);
   }
   ```

5. **Service** (`AuthService.java`):
   ```java
   public AuthResponse authenticate(LoginRequest request) {
     // Find user in database
     User user = userRepository.findByEmail(request.getEmail())
       .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
     
     // Verify password
     if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
       throw new BadCredentialsException("Invalid credentials");
     }
     
     // Generate JWT token
     String token = jwtTokenProvider.generateToken(user);
     
     return new AuthResponse(token, user);
   }
   ```

6. **Database** (Supabase PostgreSQL):
   ```sql
   SELECT * FROM users WHERE email = 'user@example.com';
   ```

7. **Response travels back** through the same layers:
   - Database → Repository → Service → Controller → HTTP → Frontend
   
8. **Frontend updates**:
   - Stores token in localStorage
   - Updates AuthContext
   - All components re-render with logged-in user

### Example 2: User Searches for Products

1. **User types in search box** → "Nike shoes"

2. **Frontend** (`LandingPage.tsx`):
   ```typescript
   const { products } = useProducts({ query: 'Nike shoes' });
   ```

3. **Custom Hook** (`api/hooks.ts`):
   ```typescript
   function useProducts({ query }) {
     return useSWR(`/products/search?query=${query}`, () => 
       apiClient.searchProducts(query)
     );
   }
   ```

4. **Backend Controller** (`ProductController.java`):
   ```java
   @GetMapping("/search")
   public List<Product> search(@RequestParam String query) {
     return productService.searchProducts(query);
   }
   ```

5. **Service** (`ProductService.java`):
   ```java
   public List<Product> searchProducts(String query) {
     return productRepository
       .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
         query, query
       );
   }
   ```

6. **Repository** (auto-generated by Spring):
   ```java
   List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
     String name, String description
   );
   ```

7. **Database** (Supabase):
   ```sql
   SELECT * FROM products 
   WHERE LOWER(name) LIKE '%nike shoes%' 
   OR LOWER(description) LIKE '%nike shoes%';
   ```

8. **Results flow back**:
   - Database → Repository → Service → Controller → HTTP → Frontend
   
9. **Frontend displays**:
   ```typescript
   products.map(product => <ProductCard product={product} />)
   ```

### Example 3: User Adds Product to Favorites

1. **User clicks heart icon** on product

2. **Frontend** (`ProductCard.tsx`):
   ```typescript
   const handleFavorite = async () => {
     await addFavorite(productId);
     mutate('/favorites');  // Refresh favorites list
   };
   ```

3. **API Call**:
   ```typescript
   await fetch('http://localhost:8080/api/favorites', {
     method: 'POST',
     headers: {
       'Authorization': `Bearer ${token}`  // JWT token included!
     },
     body: JSON.stringify({ productId })
   });
   ```

4. **Security Filter** (`JwtAuthenticationFilter.java`):
   ```java
   // Intercepts request BEFORE it reaches controller
   String token = extractToken(request);
   if (jwtTokenProvider.validateToken(token)) {
     // Extract user from token
     String email = jwtTokenProvider.getEmailFromToken(token);
     // Set authentication context
     SecurityContextHolder.getContext().setAuthentication(...);
   }
   ```

5. **Backend Controller** (`FavoriteController.java`):
   ```java
   @PostMapping
   public Favorite addFavorite(
     @RequestBody FavoriteRequest request,
     @AuthenticationPrincipal User currentUser  // Spring injects current user!
   ) {
     return favoriteService.addFavorite(currentUser.getId(), request.getProductId());
   }
   ```

6. **Service** (`FavoriteService.java`):
   ```java
   public Favorite addFavorite(UUID userId, UUID productId) {
     // Check if already favorited
     if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
       throw new AlreadyExistsException("Already in favorites");
     }
     
     Favorite favorite = new Favorite();
     favorite.setUserId(userId);
     favorite.setProductId(productId);
     
     return favoriteRepository.save(favorite);
   }
   ```

7. **Database**:
   ```sql
   INSERT INTO favorites (id, user_id, product_id, created_at)
   VALUES (uuid_generate_v4(), '...', '...', NOW());
   ```

8. **Frontend updates**:
   - SWR automatically refetches favorites
   - Heart icon changes to "filled"
   - Favorites count updates

---

## 🚀 Request/Response Lifecycle

### Complete Flow Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│  BROWSER (React App)                                             │
│                                                                  │
│  [User clicks button] → triggers event handler                  │
│           ↓                                                      │
│  [React Component] → calls API client function                  │
│           ↓                                                      │
│  [API Client] → creates fetch() request with JSON               │
│           ↓                                                      │
│  [HTTP Request] → sent over network                             │
└────────────────────┬─────────────────────────────────────────────┘
                     │
                     │ Internet/Network
                     │
┌────────────────────▼─────────────────────────────────────────────┐
│  SERVER (Spring Boot App)                                        │
│                                                                  │
│  [Tomcat] → receives HTTP request                               │
│       ↓                                                          │
│  [Security Filter] → checks authentication (JWT)                │
│       ↓                                                          │
│  [Controller] → receives request, extracts parameters           │
│       ↓                                                          │
│  [Service] → business logic, validation                         │
│       ↓                                                          │
│  [Repository] → database query                                  │
│       ↓                                                          │
│  [Database] → executes SQL, returns rows                        │
│       ↑                                                          │
│  [Repository] → converts rows to Java objects                   │
│       ↑                                                          │
│  [Service] → transforms/processes data                          │
│       ↑                                                          │
│  [Controller] → converts objects to JSON response               │
│       ↑                                                          │
│  [Tomcat] → sends HTTP response                                 │
└────────────────────┬─────────────────────────────────────────────┘
                     │
                     │ Internet/Network
                     │
┌────────────────────▼─────────────────────────────────────────────┐
│  BROWSER (React App)                                             │
│                                                                  │
│  [fetch() receives response] → parses JSON                      │
│           ↓                                                      │
│  [API Client] → returns data to component                       │
│           ↓                                                      │
│  [React Component] → updates state (useState/SWR)               │
│           ↓                                                      │
│  [React] → re-renders component with new data                   │
│           ↓                                                      │
│  [DOM] → browser displays updated UI                            │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📚 Summary

### What You've Learned

1. **Architecture**: 3-tier application (Frontend, Backend, Database)

2. **Frontend**: React SPA with TypeScript, communicates via REST API

3. **Backend**: Spring Boot with layered architecture (Controller → Service → Repository)

4. **Database**: PostgreSQL hosted on Supabase, connected via JPA/Hibernate

5. **Authentication**: JWT token-based authentication with Spring Security

6. **Communication**: RESTful HTTP requests/responses with JSON

7. **Data Flow**: User action → Frontend → API call → Backend → Database → Response → UI update

### Key Technologies

- **React**: UI library
- **Spring Boot**: Backend framework
- **PostgreSQL/Supabase**: Database
- **JWT**: Authentication tokens
- **JPA/Hibernate**: Database ORM
- **REST API**: Communication protocol

---

## 🤔 Common Questions

**Q: Why separate frontend and backend?**
A: Separation of concerns. Frontend handles UI/UX, backend handles business logic and data. They can be developed, tested, and deployed independently.

**Q: Why use JWT instead of sessions?**
A: JWTs are stateless (server doesn't store them), scalable (works across multiple servers), and mobile-friendly (no cookies needed).

**Q: Why use TypeScript instead of JavaScript?**
A: Type safety catches errors before runtime, better IDE support (autocomplete), easier refactoring, and better documentation.

**Q: Why Spring Boot instead of Node.js?**
A: Java/Spring Boot offers strong typing, mature ecosystem, better for large teams, enterprise-ready, and excellent database integration.

**Q: How does Supabase fit in?**
A: Supabase provides a managed PostgreSQL database. Your Spring Boot app connects to it like any PostgreSQL database. Supabase just handles hosting, backups, and scaling.

**Q: What happens if JWT token expires?**
A: User gets logged out automatically. They need to log in again to get a fresh token. You can implement refresh tokens for better UX.

---

This architecture documentation should give you a comprehensive understanding of how everything works together. Let me know if you'd like me to explain any specific part in more detail!
