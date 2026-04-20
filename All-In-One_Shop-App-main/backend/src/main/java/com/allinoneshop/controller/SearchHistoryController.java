package com.allinoneshop.controller;

import com.allinoneshop.dto.ApiResponse;
import com.allinoneshop.entity.User;
import com.allinoneshop.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search history and suggestions endpoints")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping("/trending")
    @Operation(summary = "Get trending search queries")
    public ResponseEntity<ApiResponse<List<String>>> getTrendingSearches(
            @RequestParam(defaultValue = "10") int limit) {
        List<String> trending = searchHistoryService.getTrendingSearches(limit);
        return ResponseEntity.ok(ApiResponse.success(trending));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get user's recent searches")
    public ResponseEntity<ApiResponse<List<String>>> getRecentSearches(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        List<String> recent = searchHistoryService.getRecentSearches(user.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(recent));
    }

    @DeleteMapping("/history")
    @Operation(summary = "Clear user's search history")
    public ResponseEntity<ApiResponse<Void>> clearSearchHistory(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        searchHistoryService.clearUserHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Search history cleared", null));
    }
}
