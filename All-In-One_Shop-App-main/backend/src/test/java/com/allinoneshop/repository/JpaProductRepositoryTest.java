package com.allinoneshop.repository;

import com.allinoneshop.entity.Brand;
import com.allinoneshop.entity.Category;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaProductRepositoryTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Product buildProduct(String name) {
        return Product.builder().name(name).gender(Gender.UNISEX).isActive(true).build();
    }

    @Test
    void save_assignsId() {
        Product saved = productRepository.save(buildProduct("Hoodie"));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_existing_returnsProduct() {
        Product saved = productRepository.save(buildProduct("Sneaker"));
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        assertThat(productRepository.findById(java.util.UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByIdWithDetails_existing_returnsProduct() {
        Product saved = productRepository.save(buildProduct("Jacket"));
        Product found = productRepository.findByIdWithDetails(saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Jacket");
    }

    @Test
    void findByIdWithDetails_nonExisting_returnsNull() {
        assertThat(productRepository.findByIdWithDetails(java.util.UUID.randomUUID())).isNull();
    }

    @Test
    void findAll_returnsAllProducts() {
        productRepository.save(buildProduct("A"));
        productRepository.save(buildProduct("B"));
        assertThat(productRepository.findAll()).hasSize(2);
    }

    @Test
    void findAllWithDetails_returnsAllProducts() {
        productRepository.save(buildProduct("X"));
        productRepository.save(buildProduct("Y"));
        productRepository.save(buildProduct("Z"));
        assertThat(productRepository.findAllWithDetails()).hasSize(3);
    }

    @Test
    void searchProducts_matchesName() {
        productRepository.save(buildProduct("Nike Running Shoe"));
        productRepository.save(buildProduct("Adidas Sneaker"));
        List<Product> results = productRepository.searchProducts("nike");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).containsIgnoringCase("nike");
    }

    @Test
    void searchProducts_matchesDescription() {
        Product p = buildProduct("Casual Top");
        p.setDescription("Perfect for summer days");
        productRepository.save(p);
        productRepository.save(buildProduct("Winter Jacket"));
        assertThat(productRepository.searchProducts("summer")).hasSize(1);
    }

    @Test
    void searchProducts_noMatch_returnsEmpty() {
        productRepository.save(buildProduct("T-Shirt"));
        assertThat(productRepository.searchProducts("nonexistent")).isEmpty();
    }

    @Test
    void findByCategorySlug_returnsMatchingProducts() {
        Category cat = categoryRepository.save(
                Category.builder().name("Shoes").slug("shoes").build());
        Product p = buildProduct("Air Max");
        p.setCategory(cat);
        productRepository.save(p);
        productRepository.save(buildProduct("Generic Product"));

        List<Product> results = productRepository.findByCategorySlug("shoes");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Air Max");
    }

    @Test
    void findByBrandName_caseInsensitive_returnsMatchingProducts() {
        Brand brand = brandRepository.save(Brand.builder().name("Puma").build());
        Product p = buildProduct("Puma Sneaker");
        p.setBrand(brand);
        productRepository.save(p);
        productRepository.save(buildProduct("No Brand"));

        assertThat(productRepository.findByBrandName("PUMA")).hasSize(1);
        assertThat(productRepository.findByBrandName("puma")).hasSize(1);
    }

    @Test
    void findSimilarProducts_returnsProductsInSameCategoryExcludingGiven() {
        Category cat = categoryRepository.save(
                Category.builder().name("Tops").slug("tops").build());
        Product p1 = buildProduct("T-Shirt"); p1.setCategory(cat); p1 = productRepository.save(p1);
        Product p2 = buildProduct("Polo");    p2.setCategory(cat); productRepository.save(p2);
        Product p3 = buildProduct("Tank");    p3.setCategory(cat); productRepository.save(p3);
        productRepository.save(buildProduct("Jeans"));

        List<Product> similar = productRepository.findSimilarProducts(cat.getId(), p1.getId(), 10);
        assertThat(similar).hasSize(2);
        assertThat(similar.stream().map(Product::getName))
                .containsExactlyInAnyOrder("Polo", "Tank");
    }

    @Test
    void findSimilarProducts_respectsLimit() {
        Category cat = categoryRepository.save(
                Category.builder().name("Bottoms").slug("bottoms").build());
        Product p1 = buildProduct("P1"); p1.setCategory(cat); p1 = productRepository.save(p1);
        for (int i = 2; i <= 6; i++) {
            Product p = buildProduct("P" + i);
            p.setCategory(cat);
            productRepository.save(p);
        }

        List<Product> similar = productRepository.findSimilarProducts(cat.getId(), p1.getId(), 2);
        assertThat(similar).hasSize(2);
    }

    @Test
    void delete_removesProduct() {
        Product saved = productRepository.save(buildProduct("To Delete"));
        productRepository.delete(saved);
        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteById_removesProduct() {
        Product saved = productRepository.save(buildProduct("To Delete 2"));
        productRepository.deleteById(saved.getId());
        assertThat(productRepository.count()).isZero();
    }

    @Test
    void count_reflectsStoredProducts() {
        assertThat(productRepository.count()).isZero();
        productRepository.save(buildProduct("A"));
        productRepository.save(buildProduct("B"));
        assertThat(productRepository.count()).isEqualTo(2);
    }
}
