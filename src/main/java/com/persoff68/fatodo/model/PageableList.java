package com.persoff68.fatodo.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PageableList<T> {
    private List<T> data;
    private long count;

    public static <T> PageableList<T> of(List<T> data, long count) {
        PageableList<T> pageableList = new PageableList<>();
        pageableList.setData(data);
        pageableList.setCount(count);
        return pageableList;
    }

    public static <T> PageableList<T> empty() {
        PageableList<T> pageableList = new PageableList<>();
        pageableList.setData(Collections.emptyList());
        pageableList.setCount(0L);
        return pageableList;
    }
}
