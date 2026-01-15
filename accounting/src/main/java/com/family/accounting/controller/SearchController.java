package com.family.accounting.controller;

import com.family.accounting.dto.PageVO;
import com.family.accounting.dto.Result;
import com.family.accounting.dto.SearchRequestDTO;
import com.family.accounting.dto.TransactionVO;
import com.family.accounting.entity.SearchHistory;
import com.family.accounting.service.SearchService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {
    
    private final SearchService searchService;
    
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * 搜索交易记录
     * 
     * @param request 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/transactions")
    public Result<PageVO<TransactionVO>> searchTransactions(@Valid @RequestBody SearchRequestDTO request) {
        PageVO<TransactionVO> result = searchService.searchTransactions(request);
        return Result.success(result);
    }
    
    /**
     * 获取搜索建议
     * 
     * @param limit 返回数量限制（默认10）
     * @return 搜索建议列表
     */
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam(defaultValue = "10") int limit) {
        List<String> suggestions = searchService.getSearchSuggestions(limit);
        return Result.success(suggestions);
    }
    
    /**
     * 获取搜索历史
     * 
     * @return 搜索历史列表
     */
    @GetMapping("/history")
    public Result<List<SearchHistory>> getSearchHistory() {
        List<SearchHistory> history = searchService.getSearchHistory();
        return Result.success(history);
    }
    
    /**
     * 删除搜索历史
     * 
     * @param id 搜索历史ID
     * @return 操作结果
     */
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteSearchHistory(@PathVariable Long id) {
        searchService.deleteSearchHistory(id);
        return Result.success();
    }
    
    /**
     * 清空搜索历史
     * 
     * @return 操作结果
     */
    @DeleteMapping("/history")
    public Result<Void> clearSearchHistory() {
        searchService.clearSearchHistory();
        return Result.success();
    }
}
