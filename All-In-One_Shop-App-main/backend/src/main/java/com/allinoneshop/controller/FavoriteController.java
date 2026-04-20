package com.allinoneshop.controller;

import com.allinoneshop.dto.*;
import com.allinoneshop.entity.User;
import com.allinoneshop.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "User favorites/wishlist management")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Get user's favorite products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFavorites(@AuthenticationPrincipal User user) {
        List<ProductDTO> favorites = favoriteService.getUserFavorites(user.getId());
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/ids")
    @Operation(summary = "Get IDs of user's favorite products")
    public ResponseEntity<ApiResponse<Set<UUID>>> getFavoriteIds(@AuthenticationPrincipal User user) {
        Set<UUID> favoriteIds = favoriteService.getFavoriteProductIds(user.getId());
        return ResponseEntity.ok(ApiResponse.success(favoriteIds));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to favorites")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable UUID productId) {
        favoriteService.addFavorite(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product added to favorites", null));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from favorites")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable UUID productId) {
        favoriteService.removeFavorite(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from favorites", null));
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "Check if product is in favorites")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable UUID productId) {
        boolean isFavorite = favoriteService.isFavorite(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success(isFavorite));
    }
}
