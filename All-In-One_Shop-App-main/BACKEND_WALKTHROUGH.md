# Backend Implementation Walkthrough

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 | Core programming language |
| **Spring Boot** | 3.2.3 | Application framework |
| **Spring Security** | (managed by Boot) | Authentication & authorization |
| **Jakarta Validation** | (managed by Boot) | Server-side request validation |
| **jjwt** | 0.12.5 | JSON Web Token generation & validation |
| **Lombok** | (managed by Boot) | Boilerplate reduction (`@Data`, `@Builder`, etc.) |
| **DataFaker** | 2.1.0 | Fake entity generation (Silver challenge) |
| **Spring WebSocket** | (managed by Boot) | STOMP over SockJS for real-time updates |
| **SpringDoc OpenAPI** | 2.3.0 | Swagger UI / API documentation |
| **JaCoCo** | 0.8.11 | Code coverage reporting |
| **JUnit 5 + AssertJ** | (via spring-boot-starter-test) | Unit testing |

---

## Architecture Overview

The backend follows a **classic 3-layer architecture** (Controller → Service → Repository), with clear separation of concerns. All data is stored **exclusively in RAM** using `ConcurrentHashMap` — there is no database of any kind.

```
com.allinoneshop
├── config/              ← Spring configuration classes
│   ├── SecurityConfig   ← HTTP security, CORS, JWT filter chain
│   └── WebSocketConfig  ← STOMP message broker setup
├── controller/          ← REST API endpoints (thin layer, delegates to services)
│   ├── AuthController
│   ├── ProductController
│   ├── BrandController
│   ├── CategoryController
│   ├── StoreController
│   ├── UserController
│   ├── FavoriteController
│   ├── SearchHistoryController
│   ├── FakerController
│   └── AdminController
├── service/             ← Business logic
│   ├── AuthService
│   ├── ProductService
│   ├── BrandService
│   ├── CategoryService
│   ├── StoreService
│   ├── UserService
│   ├── FavoriteService
│   ├── SearchHistoryService
│   ├── FakerService
│   ├── AdminService
│   └── CustomUserDetailsService
├── repository/          ← In-memory data access (ConcurrentHashMap)
│   ├── ProductRepository
│   ├── BrandRepository
│   ├── CategoryRepository
│   ├── StoreRepository
│   ├── UserRepository
│   ├── FavoriteRepository
│   ├── SearchHistoryRepository
│   └── ProductPriceRepository
├── entity/              ← Domain model (POJOs)
│   ├── Product, Brand, Category, Store, User
│   ├── ProductPrice, Favorite, SearchHistory
│   └── enums/ (Gender, Role)
├── dto/                 ← Data Transfer Objects (with validation)
│   ├── ProductDTO, BrandDTO, CategoryDTO, StoreDTO
│   ├── UserDTO, ApiResponse, PagedResponse, SearchRequest
│   └── auth/ (LoginRequest, RegisterRequest, AuthResponse)
├── security/            ← JWT infrastructure
│   ├── JwtTokenProvider
│   └── JwtAuthenticationFilter
└── exception/
    └── GlobalExceptionHandler
```

---

## In-Memory Storage (No Database)

All repositories use `ConcurrentHashMap<UUID, Entity>` for thread-safe, in-memory storage. There is **no JPA, no Hibernate, no database driver** — the `application.yml` explicitly excludes all database auto-configuration:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
```

### Repository Pattern Example (`ProductRepository`)

```java
@Repository
public class ProductRepository {
    private final ConcurrentHashMap<UUID, Product> store = new ConcurrentHashMap<>();

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
            product.setCreatedAt(OffsetDateTime.now());
        }
        product.setUpdatedAt(OffsetDateTime.now());
        store.put(product.getId(), product);
        return product;
    }

    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(Product product) {
        store.remove(product.getId());
    }

    // Additional query methods use Java Streams to filter in-memory
    public List<Product> searchProducts(String query) {
        String lowerQuery = query.toLowerCase();
        return store.values().stream()
            .filter(p -> p.getName().toLowerCase().contains(lowerQuery)
                      || (p.getDescription() != null && ...))
            .collect(Collectors.toList());
    }
}
```

All 8 repositories follow this same pattern. The key design decisions:
- **`ConcurrentHashMap`** ensures thread safety for concurrent HTTP requests
- **UUID primary keys** are auto-generated on `save()` if null
- **Timestamps** (`createdAt`, `updatedAt`) are set automatically
- **Query methods** (search, filter by category/brand, etc.) use Java Streams over the map values

---

## Entity Model

The domain model consists of 8 entities (plain Java POJOs with Lombok annotations):

```
Product ──── Brand (many-to-one)
   │  └───── Category (many-to-one)
   │  └───── ProductPrice[] (one-to-many)
   │              └── Store (many-to-one)
   │
User ──── Favorite[] ──── Product
   │
   └── SearchHistory[]
```

### Key Entity: `User` implements `UserDetails`

The `User` entity implements Spring Security's `UserDetails` interface, enabling direct integration with the security framework:

```java
public class User implements UserDetails {
    private UUID id;
    private String email;
    private String passwordHash;
    private String firstName, lastName;
    private Role role;   // enum: USER, ADMIN

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }  // email is the username
}
```

### Key Entity: `Product`

```java
public class Product {
    private UUID id;
    private String name;
    private String description;
    private Brand brand;               // associated brand
    private Category category;         // associated category
    private String imageUrl;
    private String[] additionalImages;
    private String[] sizes;            // e.g. ["S", "M", "L", "XL"]
    private String[] colors;           // e.g. ["Red", "Blue"]
    private List<ProductPrice> prices; // prices from different stores
    private Gender gender;             // enum: MEN, WOMEN, UNISEX
    private Boolean isActive;
    private OffsetDateTime createdAt, updatedAt;
}
```

---

## REST API Endpoints

All endpoints are prefixed with `/api` (via `server.servlet.context-path: /api`).

### Authentication (`/auth`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register with email + password (returns JWT) |
| POST | `/auth/login` | Public | Login with email + password (returns JWT) |
| GET | `/auth/me` | Authenticated | Get current user profile |

### Products (`/products`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/products` | Public | Get all products (paginated with filters) |
| GET | `/products/{id}` | Public | Get product by ID |
| POST | `/products/search` | Public | Search with filters (paginated) |
| GET | `/products/category/{slug}` | Public | Products by category |
| GET | `/products/brand/{name}` | Public | Products by brand |
| GET | `/products/{id}/similar` | Public | Similar products |
| GET | `/products/trending` | Public | Trending products |
| POST | `/products` | Admin | Create a product |
| PUT | `/products/{id}` | Admin | Update a product |
| DELETE | `/products/{id}` | Admin | Delete a product |

### Brands (`/brands`), Categories (`/categories`), Stores (`/stores`)
Each follows the same CRUD pattern:
| Method | Endpoint | Access |
|---|---|---|
| GET | `/{resource}` | Public |
| GET | `/{resource}/{id}` | Public |
| POST | `/{resource}` | Admin |
| PUT | `/{resource}/{id}` | Admin |
| DELETE | `/{resource}/{id}` | Admin |

### Favorites (`/favorites`)
| Method | Endpoint | Access |
|---|---|---|
| GET | `/favorites` | Authenticated |
| GET | `/favorites/ids` | Authenticated |
| POST | `/favorites/{productId}` | Authenticated |
| DELETE | `/favorites/{productId}` | Authenticated |
| GET | `/favorites/{productId}/check` | Authenticated |

### Search History (`/search`)
| Method | Endpoint | Access |
|---|---|---|
| GET | `/search/trending` | Public |
| GET | `/search/recent` | Authenticated |
| DELETE | `/search/history` | Authenticated |

### Faker (`/faker`) — Silver Challenge
| Method | Endpoint | Description |
|---|---|---|
| POST | `/faker/start?intervalMs=3000&batchSize=5` | Start async fake product generation |
| POST | `/faker/stop` | Stop fake generation |
| GET | `/faker/status` | Check if generation is running |

---

## Server-Side Validation

Validation is implemented using **Jakarta Bean Validation** annotations on DTOs, triggered by `@Valid` on controller method parameters.

### Example: `ProductDTO`
```java
public class ProductDTO {
    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 500, message = "Product name must be between 1 and 500 characters")
    private String name;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;
    // ...
}
```

### Validated DTOs

| DTO | Validated Fields |
|---|---|
| `ProductDTO` | `name` (required, 1-500 chars), `description` (max 5000), `imageUrl` (max 500) |
| `BrandDTO` | `name` (required, 1-255 chars), `logoUrl` (max 500) |
| `CategoryDTO` | `name` (required, 1-255 chars), `slug` (max 255) |
| `StoreDTO` | `name` (required, 1-255 chars), `website` (required, max 500), `logoUrl` (max 500) |
| `RegisterRequest` | `email` (required, valid format), `password` (required, min 8 chars) |
| `LoginRequest` | `email` (required, valid format), `password` (required) |

### Global Exception Handler

`GlobalExceptionHandler` (annotated with `@RestControllerAdvice`) catches validation errors and returns structured responses:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(...) {
    // Collects all field errors into a map: { "name": "Product name is required", ... }
    // Returns 400 Bad Request with { success: false, message: "Validation failed", data: errors }
}
```

It also handles:
- `RuntimeException` → 400 Bad Request
- `BadCredentialsException` → 401 Unauthorized
- `AuthenticationException` → 401 Unauthorized
- `Exception` (catch-all) → 500 Internal Server Error

---

## Server-Side Pagination

Implemented in `ProductService.searchProductsPaged()`:

```java
public PagedResponse<ProductDTO> searchProductsPaged(SearchRequest request, UUID userId) {
    List<ProductDTO> allResults = searchProducts(request, userId);

    int page = request.getPage() != null ? request.getPage() : 0;
    int size = request.getSize() != null ? request.getSize() : 20;

    int totalElements = allResults.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    int fromIndex = Math.min(page * size, totalElements);
    int toIndex = Math.min(fromIndex + size, totalElements);

    List<ProductDTO> pageContent = allResults.subList(fromIndex, toIndex);

    return PagedResponse.<ProductDTO>builder()
        .content(pageContent)
        .totalElements(totalElements)
        .totalPages(totalPages)
        .page(page)
        .size(size)
        .build();
}
```

The response DTO:
```java
public class PagedResponse<T> {
    private List<T> content;    // current page items
    private int totalElements;  // total count across all pages
    private int totalPages;     // number of pages
    private int page;           // current page (0-indexed)
    private int size;           // items per page
}
```

---

## Authentication & Security

### JWT Flow

1. **Register/Login** → `AuthService` creates/validates user → `JwtTokenProvider.generateToken()` creates a signed JWT
2. **Subsequent requests** → `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) extracts the JWT from `Authorization: Bearer <token>`, validates it, loads the user via `CustomUserDetailsService`, and sets the SecurityContext
3. **Protected endpoints** use `@PreAuthorize("hasRole('ADMIN')")` or are configured in `SecurityConfig`

### Token Structure
```java
Jwts.builder()
    .subject(user.getEmail())            // email as subject
    .claim("userId", user.getId())       // user ID as custom claim
    .issuedAt(now)
    .expiration(now + 24h)               // 24-hour expiry
    .signWith(hmacKey)                   // HMAC-SHA signing
    .compact();
```

### Password Security
Passwords are hashed using **BCrypt** via `BCryptPasswordEncoder`.

### Security Configuration Summary
- CSRF disabled (stateless API)
- Sessions: STATELESS
- CORS: all origins allowed (development)
- Public: `/auth/**`, GET on products/brands/categories/stores, `/ws/**`, `/faker/**`, Swagger
- Authenticated: `/favorites/**`, `/users/**`
- Admin only: POST/PUT/DELETE on resources, `/admin/stats`

---

## Faker & WebSocket (Silver Challenge)

### FakerService — Async Entity Generation

`FakerService` creates an async loop on a daemon thread that:
1. Generates `batchSize` fake products per tick using the **DataFaker** library
2. Each product includes a randomly generated brand, category, store, and price
3. Products are saved to the in-memory repositories
4. After each batch, a message is sent via **WebSocket** to `/topic/products`
5. Loop continues every `intervalMs` until `stopGenerating()` is called

```java
Thread generatorThread = new Thread(() -> {
    while (generating.get()) {
        List<ProductDTO> generated = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            generated.add(generateFakeProduct());
        }

        Map<String, Object> message = Map.of(
            "type", "BATCH_ADDED",
            "count", generated.size(),
            "products", generated,
            "totalProducts", productRepository.count(),
            "timestamp", System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/products", message);
        Thread.sleep(intervalMs);
    }
});
```

### WebSocket Configuration

Uses STOMP over SockJS:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");          // subscribe destinations
        config.setApplicationDestinationPrefixes("/app"); // send destinations
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

---

## API Response Envelope

All API responses use a consistent `ApiResponse<T>` wrapper:

```java
public class ApiResponse<T> {
    private boolean success;   // true for success, false for errors
    private String message;    // optional human-readable message
    private T data;            // the actual payload

    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> success(String message, T data) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
}
```

Example success response:
```json
{
  "success": true,
  "message": null,
  "data": { "id": "...", "name": "Nike Hoodie", ... }
}
```

Example error response:
```json
{
  "success": false,
  "message": "Product not found",
  "data": null
}
```

---

## Unit Testing

### Test Strategy

All 9 service classes have dedicated test suites using **JUnit 5** + **AssertJ**. Tests instantiate real in-memory repositories (not mocks) to verify business logic end-to-end within the service layer. Only external dependencies like `JwtTokenProvider` and `AuthenticationManager` are mocked (in `AuthServiceTest`).

### Test Suites

| Test Class | # Tests | Coverage |
|---|---|---|
| `ProductServiceTest` | ~30 | CRUD, search, filtering (brand, category, price, size, color), sorting (4 modes), pagination (3 cases), similar products, trending |
| `BrandServiceTest` | 8 | CRUD + duplicate name prevention |
| `CategoryServiceTest` | 10 | CRUD + auto-slug generation + slug lookup |
| `StoreServiceTest` | 8 | CRUD + auto-active on create |
| `UserServiceTest` | 6 | Profile get/update (full, partial, null safety) |
| `AuthServiceTest` | 8 | Register, login, password hashing, duplicate email, JWT integration |
| `FavoriteServiceTest` | 8 | Add, remove, duplicate prevention, user/product not found, list, IDs |
| `SearchHistoryServiceTest` | 6 | Save, recent (with limit), trending by frequency, clear (user isolation) |
| `AdminServiceTest` | 7 | Dashboard stats, product ingestion, price creation, deduplication |

### Code Coverage

JaCoCo is configured in `pom.xml` to generate a coverage report during the `test` phase. Run:
```bash
cd backend
./mvnw.cmd test
```
Then view the report at `backend/target/site/jacoco/index.html`.
