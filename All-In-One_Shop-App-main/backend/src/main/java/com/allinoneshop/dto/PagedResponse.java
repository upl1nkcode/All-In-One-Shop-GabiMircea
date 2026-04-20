package com.allinoneshop.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int page;
    private int size;
}
