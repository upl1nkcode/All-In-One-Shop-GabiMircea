package com.allinoneshop.controller;

import com.allinoneshop.dto.ApiResponse;
import com.allinoneshop.service.FakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/faker")
@RequiredArgsConstructor
@Tag(name = "Faker", description = "Fake entity generation endpoints (Silver challenge)")
public class FakerController {

    private final FakerService fakerService;

    @PostMapping("/start")
    @Operation(summary = "Start generating fake products asynchronously")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startGenerating(
            @RequestParam(defaultValue = "3000") int intervalMs,
            @RequestParam(defaultValue = "5") int batchSize) {
        fakerService.startGenerating(intervalMs, batchSize);
        return ResponseEntity.ok(ApiResponse.success("Fake generation started",
                Map.of("intervalMs", intervalMs, "batchSize", batchSize)));
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop generating fake products")
    public ResponseEntity<ApiResponse<Void>> stopGenerating() {
        fakerService.stopGenerating();
        return ResponseEntity.ok(ApiResponse.success("Fake generation stopped", null));
    }

    @GetMapping("/status")
    @Operation(summary = "Check if fake generation is running")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("generating", fakerService.isGenerating())));
    }
}
