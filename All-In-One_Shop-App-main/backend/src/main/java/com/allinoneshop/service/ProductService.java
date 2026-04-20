package com.allinoneshop.service;

import com.allinoneshop.dto.*;
import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public List<ProductDTO> searchProducts(SearchRequest request, UUID userId) {
        List<Product> products;

        if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            products = productRepository.searchProducts(request.getQuery().trim());
            saveSearchHistory(request.getQuery(), products.size(), userId);
        } else {
            products = productRepository.findAllWithDetails();
        }

        products = applyFilters(products, request);
        products = applySorting(products, request.getSortBy());

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

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

    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findByIdWithDetails(id);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        return convertToDTO(product);
    }

    public List<ProductDTO> getProductsByCategory(String categorySlug) {
        return productRepository.findByCategorySlug(categorySlug).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByBrand(String brandName) {
        return productRepository.findByBrandName(brandName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getSimilarProducts(UUID productId, int limit) {
        Product product = productRepository.findByIdWithDetails(productId);
        if (product == null || product.getCategory() == null) {
            return Collections.emptyList();
        }

        return productRepository.findSimilarProducts(
                product.getCategory().getId(),
                productId,
                limit
        ).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getTrendingProducts(int limit) {
        return productRepository.findAllWithDetails().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    public ProductDTO updateProduct(UUID id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        applyDtoToProduct(product, dto);
        return convertToDTO(productRepository.save(product));
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = new Product();
        product.setIsActive(true);
        product.setPrices(new ArrayList<>());
        applyDtoToProduct(product, dto);
        return convertToDTO(productRepository.save(product));
    }

    private void applyDtoToProduct(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getGender() != null) {
            try { product.setGender(Gender.valueOf(dto.getGender().toUpperCase())); }
            catch (Exception e) { product.setGender(Gender.UNISEX); }
        }
        if (dto.getBrandId() != null) {
            brandRepository.findById(dto.getBrandId()).ifPresent(product::setBrand);
        } else if (dto.getBrand() != null && dto.getBrand().getId() != null) {
            brandRepository.findById(dto.getBrand().getId()).ifPresent(product::setBrand);
        }
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId()).ifPresent(product::setCategory);
        } else if (dto.getCategory() != null && dto.getCategory().getId() != null) {
            categoryRepository.findById(dto.getCategory().getId()).ifPresent(product::setCategory);
        }
        if (dto.getSizes() != null) product.setSizes(dto.getSizes());
        if (dto.getColors() != null) product.setColors(dto.getColors());
    }

    private void saveSearchHistory(String query, int resultsCount, UUID userId) {
        SearchHistory history = SearchHistory.builder()
                .searchQuery(query)
                .resultsCount(resultsCount)
                .build();

        if (userId != null) {
            userRepository.findById(userId).ifPresent(history::setUser);
        }

        searchHistoryRepository.save(history);
    }

    private List<Product> applyFilters(List<Product> products, SearchRequest request) {
        return products.stream()
                .filter(p -> filterByBrand(p, request.getBrandIds()))
                .filter(p -> filterByCategory(p, request.getCategoryIds()))
                .filter(p -> filterByStore(p, request.getStoreIds()))
                .filter(p -> filterByPrice(p, request.getMinPrice(), request.getMaxPrice()))
                .filter(p -> filterBySize(p, request.getSizes()))
                .filter(p -> filterByColor(p, request.getColors()))
                .collect(Collectors.toList());
    }

    private boolean filterByBrand(Product product, List<UUID> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) return true;
        return product.getBrand() != null && brandIds.contains(product.getBrand().getId());
    }

    private boolean filterByCategory(Product product, List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return true;
        return product.getCategory() != null && categoryIds.contains(product.getCategory().getId());
    }

    private boolean filterByStore(Product product, List<UUID> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return true;
        return product.getPrices() != null && product.getPrices().stream()
                .anyMatch(pp -> storeIds.contains(pp.getStore().getId()));
    }

    private boolean filterByPrice(Product product, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) return true;
        if (product.getPrices() == null || product.getPrices().isEmpty()) return false;

        BigDecimal lowestPrice = product.getPrices().stream()
                .map(ProductPrice::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (minPrice != null && lowestPrice.compareTo(minPrice) < 0) return false;
        if (maxPrice != null && lowestPrice.compareTo(maxPrice) > 0) return false;
        return true;
    }

    private boolean filterBySize(Product product, List<String> sizes) {
        if (sizes == null || sizes.isEmpty()) return true;
        if (product.getSizes() == null) return false;
        return Arrays.stream(product.getSizes())
                .anyMatch(s -> sizes.stream().anyMatch(fs -> fs.equalsIgnoreCase(s)));
    }

    private boolean filterByColor(Product product, List<String> colors) {
        if (colors == null || colors.isEmpty()) return true;
        if (product.getColors() == null) return false;
        return Arrays.stream(product.getColors())
                .anyMatch(c -> colors.stream().anyMatch(fc -> fc.equalsIgnoreCase(c)));
    }

    private List<Product> applySorting(List<Product> products, String sortBy) {
        if (sortBy == null) return products;

        Comparator<Product> comparator;
        switch (sortBy.toLowerCase()) {
            case "price_asc":
                comparator = Comparator.comparing(this::getLowestPrice);
                break;
            case "price_desc":
                comparator = Comparator.comparing(this::getLowestPrice, Comparator.reverseOrder());
                break;
            case "name_asc":
                comparator = Comparator.comparing(Product::getName);
                break;
            case "name_desc":
                comparator = Comparator.comparing(Product::getName, Comparator.reverseOrder());
                break;
            default:
                comparator = null;
                break;
        }

        if (comparator != null) {
            return products.stream().sorted(comparator).collect(Collectors.toList());
        }
        return products;
    }

    private BigDecimal getLowestPrice(Product product) {
        if (product.getPrices() == null || product.getPrices().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return product.getPrices().stream()
                .map(ProductPrice::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public ProductDTO convertToDTO(Product product) {
        List<ProductPriceDTO> priceDTOs = new ArrayList<>();
        BigDecimal lowestPrice = null;
        BigDecimal highestPrice = null;

        if (product.getPrices() != null && !product.getPrices().isEmpty()) {
            priceDTOs = product.getPrices().stream()
                    .map(this::convertPriceToDTO)
                    .sorted(Comparator.comparing(ProductPriceDTO::getPrice))
                    .collect(Collectors.toList());

            lowestPrice = priceDTOs.get(0).getPrice();
            highestPrice = priceDTOs.get(priceDTOs.size() - 1).getPrice();
        }

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .additionalImages(product.getAdditionalImages())
                .sizes(product.getSizes())
                .colors(product.getColors())
                .gender(product.getGender() != null ? product.getGender().name() : null)
                .isActive(product.getIsActive())
                .brand(product.getBrand() != null ? convertBrandToDTO(product.getBrand()) : null)
                .category(product.getCategory() != null ? convertCategoryToDTO(product.getCategory()) : null)
                .prices(priceDTOs)
                .lowestPrice(lowestPrice)
                .highestPrice(highestPrice)
                .storeCount(priceDTOs.size())
                .build();
    }

    private ProductPriceDTO convertPriceToDTO(ProductPrice price) {
        BigDecimal savings = null;
        if (price.getOriginalPrice() != null) {
            savings = price.getOriginalPrice().subtract(price.getPrice());
        }

        return ProductPriceDTO.builder()
                .id(price.getId())
                .store(convertStoreToDTO(price.getStore()))
                .price(price.getPrice())
                .originalPrice(price.getOriginalPrice())
                .currency(price.getCurrency())
                .productUrl(price.getProductUrl())
                .inStock(price.getInStock())
                .savings(savings)
                .build();
    }

    private StoreDTO convertStoreToDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .website(store.getWebsite())
                .logoUrl(store.getLogoUrl())
                .isActive(store.getIsActive())
                .build();
    }

    private BrandDTO convertBrandToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .build();
    }

    private CategoryDTO convertCategoryToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }
}
