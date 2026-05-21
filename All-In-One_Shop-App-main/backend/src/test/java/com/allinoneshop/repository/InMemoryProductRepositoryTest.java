package com.allinoneshop.repository;

import com.allinoneshop.entity.Brand;
import com.allinoneshop.entity.Category;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductRepository();
    }

    private Product buildProduct(String name) {
        return Product.builder().name(name).gender(Gender.UNISEX).isActive(true).build();
    }

    @Test
    void save_generatesId() {
        Product saved = repo.save(buildProduct("Hoodie"));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_setsTimestamps() {
        Product saved = repo.save(buildProduct("T-Shirt"));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_existing_returnsProduct() {
        Product saved = repo.save(buildProduct("Sneaker"));
        assertThat(repo.findById(saved.getId())).isPresent();
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        assertThat(repo.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByIdWithDetails_existing_returnsProduct() {
        Product saved = repo.save(buildProduct("Jacket"));
        Product found = repo.findByIdWithDetails(saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Jacket");
    }

    @Test
    void findByIdWithDetails_nonExisting_returnsNull() {
        assertThat(repo.findByIdWithDetails(UUID.randomUUID())).isNull();
    }

    @Test
    void findAllWithDetails_returnsAllProducts() {
        repo.save(buildProduct("A"));
        repo.save(buildProduct("B"));
        repo.save(buildProduct("C"));
        assertThat(repo.findAllWithDetails()).hasSize(3);
    }

    @Test
    void searchProducts_matchesName() {
        repo.save(buildProduct("Nike Running Shoe"));
        repo.save(buildProduct("Adidas Sneaker"));
        List<Product> results = repo.searchProducts("nike");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).containsIgnoringCase("nike");
    }

    @Test
    void searchProducts_matchesDescription() {
        Product p = buildProduct("Casual Top");
        p.setDescription("Perfect for summer days");
        repo.save(p);
        repo.save(buildProduct("Winter Jacket"));
        List<Product> results = repo.searchProducts("summer");
        assertThat(results).hasSize(1);
    }

    @Test
    void searchProducts_noMatch_returnsEmpty() {
        repo.save(buildProduct("T-Shirt"));
        assertThat(repo.searchProducts("nonexistent")).isEmpty();
    }

    @Test
    void findByCategorySlug_returnsMatchingProducts() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Shoes").slug("shoes").build();
        Product p = buildProduct("Air Max");
        p.setCategory(cat);
        repo.save(p);
        repo.save(buildProduct("Generic Product"));

        List<Product> results = repo.findByCategorySlug("shoes");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Air Max");
    }

    @Test
    void findByBrandName_caseInsensitive_returnsMatchingProducts() {
        Brand brand = Brand.builder().id(UUID.randomUUID()).name("Puma").build();
        Product p = buildProduct("Puma Sneaker");
        p.setBrand(brand);
        repo.save(p);
        repo.save(buildProduct("No Brand"));

        assertThat(repo.findByBrandName("PUMA")).hasSize(1);
        assertThat(repo.findByBrandName("puma")).hasSize(1);
    }

    @Test
    void findSimilarProducts_returnsProductsInSameCategoryExcludingGiven() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Tops").slug("tops").build();
        Product p1 = buildProduct("T-Shirt"); p1.setCategory(cat); p1 = repo.save(p1);
        Product p2 = buildProduct("Polo");    p2.setCategory(cat); p2 = repo.save(p2);
        Product p3 = buildProduct("Tank");    p3.setCategory(cat); p3 = repo.save(p3);
        Product other = buildProduct("Jeans"); repo.save(other);

        List<Product> similar = repo.findSimilarProducts(cat.getId(), p1.getId(), 10);
        assertThat(similar).hasSize(2);
        assertThat(similar.stream().map(Product::getName))
                .containsExactlyInAnyOrder("Polo", "Tank");
    }

    @Test
    void findSimilarProducts_respectsLimit() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Bottoms").slug("bottoms").build();
        Product p1 = buildProduct("P1"); p1.setCategory(cat); p1 = repo.save(p1);
        for (int i = 0; i < 5; i++) {
            Product p = buildProduct("P" + (i + 2));
            p.setCategory(cat);
            repo.save(p);
        }

        List<Product> similar = repo.findSimilarProducts(cat.getId(), p1.getId(), 2);
        assertThat(similar).hasSize(2);
    }

    @Test
    void delete_removesProduct() {
        Product saved = repo.save(buildProduct("To Delete"));
        repo.delete(saved);
        assertThat(repo.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteById_removesProduct() {
        Product saved = repo.save(buildProduct("To Delete 2"));
        repo.deleteById(saved.getId());
        assertThat(repo.count()).isZero();
    }

    @Test
    void count_reflectsStoredProducts() {
        assertThat(repo.count()).isZero();
        repo.save(buildProduct("A"));
        repo.save(buildProduct("B"));
        assertThat(repo.count()).isEqualTo(2);
    }
}
