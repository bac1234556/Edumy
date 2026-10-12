package com.edumy.backend.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class PagedResultDto<T> {
    private List<T> items = new ArrayList<>();
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalItems;
    private Integer totalPages;

    public boolean isHasPreviousPage() {
        return pageNumber > 1;
    }

    public boolean isHasNextPage() {
        return pageNumber < totalPages;
    }
}
