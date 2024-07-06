package com.project.legendsofleague.domain.item.dto.page;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@ToString
public class PageResponseDto<T> {

    private List<T> content;
    private int totalPage;

    private int page;
    private int size;
    private long totalCount;
    private int start, end;
    private boolean prev, next;
    private List<Integer> pageList;


    public PageResponseDto(Pageable pageable, Page<T> pageInfo) {
        content = pageInfo.getContent();
        totalPage = pageInfo.getTotalPages();
        totalCount = pageInfo.getTotalElements();
        makePageList(pageable);
    }


    private void makePageList(Pageable pageable) {
        this.page = pageable.getPageNumber() + 1;
        this.size = pageable.getPageSize();

        int tempEnd = (int) (Math.ceil(page / (double) size)) * size;

        start = tempEnd - (size - 1);
        end = Math.min(totalPage, tempEnd);
        // end = (6, 10) = 6

        prev = start > 1;
        next = totalPage > tempEnd;

        pageList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }
}
