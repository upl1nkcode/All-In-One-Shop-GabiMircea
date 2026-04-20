# AllInOne Shop - Spring Boot Backend

A Java Spring Boot REST API for the AllInOne Shop meta-search fashion e-commerce platform.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **Lombok** for reducing boilerplate
- **SpringDoc OpenAPI** for API documentation

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database (Supabase)

## Configuration

### Environment Variables

Create a `.env` file or set these environment variables:

```bash
# Database (Supabase)
SUPABASE_DB_HOST=db.xxxxxxxx.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your-database-password

# JWT
JWT_SECRET=your-256-bit-secret-key-make-it-at-least-32-characters-long

# CORS (Frontend URLs)
CORS_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Getting Supabase Connection Details

1. Go to your Supabase project dashboard
2. Navigate to **Settings** > **Database**
3. Find the connection details under "Connection string" > "JDBC"
4. Extract the host, port, database name, user, and password

## Running the Application

### Using Maven

```bash
# Navigate to backend directory
cd backend

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using IDE

Import the project as a Maven project and run `AllInOneShopApplication.java`

## API Documentation

Once running, access the Swagger UI at:
- http://localhost:8080/api/swagger-ui.html

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |
| GET | `/api/auth/me` | Get current user (auth required) |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products |
| POST | `/api/products/search` | Search with filters |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/category/{slug}` | Get products by category |
| GET | `/api/products/brand/{name}` | Get products by brand |
| GET | `/api/products/{id}/similar` | Get similar products |
| GET | `/api/products/trending` | Get trending products |

### Catalog
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| GET | `/api/brands` | Get all brands |
| GET | `/api/stores` | Get all stores |

### Favorites (Auth Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/favorites` | Get user's favorites |
| GET | `/api/favorites/ids` | Get favorite product IDs |
| POST | `/api/favorites/{productId}` | Add to favorites |
| DELETE | `/api/favorites/{productId}` | Remove from favorites |
| GET | `/api/favorites/{productId}/check` | Check if in favorites |

## Search Request Example

```json
{
  "query": "nike shoes",
  "brandIds": ["uuid-1", "uuid-2"],
  "categoryIds": ["uuid-3"],
  "storeIds": ["uuid-4"],
  "minPrice": 50.00,
  "maxPrice": 200.00,
  "sizes": ["42", "43"],
  "colors": ["Black", "White"],
  "sortBy": "price_asc",
  "page": 0,
  "size": 20
}
```

## Authentication

Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Project Structure

```
backend/
├── src/main/java/com/allinoneshop/
│   ├── AllInOneShopApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── CatalogController.java
│   │   ├── FavoriteController.java
│   │   └── ProductController.java
│   ├── dto/
│   │   ├── ApiResponse.java
│   │   ├── ProductDTO.java
│   │   ├── SearchRequest.java
│   │   └── auth/
│   │       ├── AuthResponse.java
│   │       ├── LoginRequest.java
│   │       └── RegisterRequest.java
│   ├── entity/
│   │   ├── Brand.java
│   │   ├── Category.java
│   │   ├── Favorite.java
│   │   ├── Product.java
│   │   ├── ProductPrice.java
│   │   ├── SearchHistory.java
│   │   ├── Store.java
│   │   └── User.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── repository/
│   │   └── *Repository.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtTokenProvider.java
│   └── service/
│       ├── AuthService.java
│       ├── CatalogService.java
│       ├── CustomUserDetailsService.java
│       ├── FavoriteService.java
│       └── ProductService.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```
