package com.family.accounting.controller;

import com.family.accounting.dto.FilterConditionVO;
import com.family.accounting.dto.Result;
import com.family.accounting.dto.SavedFilterDTO;
import com.family.accounting.dto.SavedFilterVO;
import com.family.accounting.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 筛选条件控制器
 */
@RestController
@RequestMapping("/api/filter")
public class FilterController {
    
    private final FilterService filterService;
    
    public FilterController(FilterService filterService) {
        this.filterService = filterService;
    }
    
    /**
     * 保存筛选条件
     * 
     * @param dto 筛选条件DTO
     * @return 保存的筛选条件
     */
    @PostMapping("/save")
    public Result<SavedFilterVO> saveFilter(@Valid @RequestBody SavedFilterDTO dto) {
        SavedFilterVO savedFilter = filterService.saveFilter(dto);
        return Result.success(savedFilter);
    }
    
    /**
     * 更新筛选条件
     * 
     * @param id 筛选条件ID
     * @param dto 筛选条件DTO
     * @return 更新后的筛选条件
     */
    @PutMapping("/{id}")
    public Result<SavedFilterVO> updateFilter(@PathVariable Long id, 
                                              @Valid @RequestBody SavedFilterDTO dto) {
        SavedFilterVO savedFilter = filterService.updateFilter(id, dto);
        return Result.success(savedFilter);
    }
    
    /**
     * 获取所有保存的筛选条件
     * 
     * @return 筛选条件列表
     */
    @GetMapping("/saved")
    public Result<List<SavedFilterVO>> getSavedFilters() {
        List<SavedFilterVO> filters = filterService.getSavedFilters();
        return Result.success(filters);
    }
    
    /**
     * 获取筛选条件详情
     * 
     * @param id 筛选条件ID
     * @return 筛选条件详情
     */
    @GetMapping("/{id}")
    public Result<SavedFilterVO> getFilterById(@PathVariable Long id) {
        SavedFilterVO filter = filterService.getFilterById(id);
        return Result.success(filter);
    }
    
    /**
     * 应用筛选条件
     * 
     * @param id 筛选条件ID
     * @return 筛选条件
     */
    @GetMapping("/{id}/apply")
    public Result<FilterConditionVO> applyFilter(@PathVariable Long id) {
        FilterConditionVO filterCondition = filterService.applyFilter(id);
        return Result.success(filterCondition);
    }
    
    /**
     * 删除筛选条件
     * 
     * @param id 筛选条件ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFilter(@PathVariable Long id) {
        filterService.deleteFilter(id);
        return Result.success();
    }
}
