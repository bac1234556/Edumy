package com.edumy.shared.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        PageMeta page
) {
    public record PageMeta(
            int number,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public static <T> PageResponse<T> from(Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                new PageMeta(
                        springPage.getNumber(),
                        springPage.getSize(),
                        springPage.getTotalElements(),
                        springPage.getTotalPages()
                )
        );
    }
}
