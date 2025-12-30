package com.family.accounting.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页视图对象
 */
@Data
public class PageVO<T> {

    /**
     * 当前页码（从1开始）
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 创建分页对象
     */
    public static <T> PageVO<T> of(List<T> list, int page, int size, long total) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setTotal(total);

        int totalPages = (int) Math.ceil((double) total / size);
        pageVO.setTotalPages(totalPages);
        pageVO.setHasNext(page < totalPages);
        pageVO.setHasPrevious(page > 1);

        return pageVO;
    }
}
