package com.family.accounting.service;

import com.family.accounting.dto.PageVO;
import com.family.accounting.dto.SearchRequestDTO;
import com.family.accounting.dto.TransactionVO;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.entity.SearchHistory;
import com.family.accounting.entity.Transaction;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.CategoryMapper;
import com.family.accounting.mapper.FamilyMemberMapper;
import com.family.accounting.mapper.SearchHistoryMapper;
import com.family.accounting.mapper.TransactionMapper;
import com.family.accounting.security.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务
 */
@Service
public class SearchService {
    
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final FamilyMemberMapper familyMemberMapper;
    
    public SearchService(TransactionMapper transactionMapper,
                        CategoryMapper categoryMapper,
                        SearchHistoryMapper searchHistoryMapper,
                        FamilyMemberMapper familyMemberMapper) {
        this.transactionMapper = transactionMapper;
        this.categoryMapper = categoryMapper;
        this.searchHistoryMapper = searchHistoryMapper;
        this.familyMemberMapper = familyMemberMapper;
    }
    
    /**
     * 搜索交易记录
     * 
     * @param request 搜索请求
     * @return 分页结果
     */
    @Transactional
    public PageVO<TransactionVO> searchTransactions(SearchRequestDTO request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        // 如果有关键词，记录搜索历史
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            recordSearchHistory(userId, familyId, request.getKeyword().trim());
        }
        
        // 处理分类筛选：如果指定了分类，需要包含所有子分类
        List<Long> expandedCategoryIds = expandCategoryIds(request.getCategoryIds());
        
        // 计算分页参数
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 20;
        int offset = (page - 1) * size;
        
        // 执行搜索
        List<Transaction> transactions = transactionMapper.searchTransactions(
                request.getAccountBookId(),
                request.getKeyword(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinAmount(),
                request.getMaxAmount(),
                expandedCategoryIds,
                request.getMemberIds(),
                request.getType(),
                offset,
                size
        );
        
        // 统计总数
        int total = transactionMapper.countSearchTransactions(
                request.getAccountBookId(),
                request.getKeyword(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinAmount(),
                request.getMaxAmount(),
                expandedCategoryIds,
                request.getMemberIds(),
                request.getType()
        );
        
        // 转换为VO
        List<TransactionVO> transactionVOs = transactions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建分页结果
        return PageVO.of(transactionVOs, page, size, (long) total);
    }
    
    /**
     * 获取搜索建议
     * 
     * @param limit 返回数量限制
     * @return 搜索历史列表
     */
    public List<String> getSearchSuggestions(int limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        List<SearchHistory> histories = searchHistoryMapper.findSuggestions(userId, familyId, limit);
        return histories.stream()
                .map(SearchHistory::getKeyword)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取搜索历史
     * 
     * @return 搜索历史列表
     */
    public List<SearchHistory> getSearchHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        return searchHistoryMapper.findByUserAndFamily(userId, familyId);
    }
    
    /**
     * 删除搜索历史
     * 
     * @param id 搜索历史ID
     */
    public void deleteSearchHistory(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        searchHistoryMapper.deleteById(id, userId);
    }
    
    /**
     * 清空搜索历史
     */
    public void clearSearchHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        searchHistoryMapper.deleteByUserAndFamily(userId, familyId);
    }
    
    /**
     * 获取当前用户的家庭ID
     * 
     * @param userId 用户ID
     * @return 家庭ID
     */
    private Long getCurrentFamilyId(Long userId) {
        FamilyMember member = familyMemberMapper.findByUserId(userId);
        if (member == null) {
            throw new BusinessException("用户未加入任何家庭");
        }
        return member.getFamilyId();
    }
    
    /**
     * 记录搜索历史
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @param keyword 关键词
     */
    private void recordSearchHistory(Long userId, Long familyId, String keyword) {
        // 查询是否已存在该关键词
        SearchHistory existing = searchHistoryMapper.findByUserFamilyAndKeyword(userId, familyId, keyword);
        
        if (existing != null) {
            // 如果存在，增加搜索次数
            searchHistoryMapper.incrementSearchCount(existing.getId());
        } else {
            // 如果不存在，创建新记录
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(userId);
            searchHistory.setFamilyId(familyId);
            searchHistory.setKeyword(keyword);
            searchHistory.setSearchCount(1);
            searchHistoryMapper.insert(searchHistory);
        }
    }
    
    /**
     * 扩展分类ID列表，包含所有子分类
     * 
     * @param categoryIds 原始分类ID列表
     * @return 扩展后的分类ID列表
     */
    private List<Long> expandCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        
        List<Long> expandedIds = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            // 获取该分类及其所有子分类的ID
            List<Long> descendantIds = categoryMapper.findAllDescendantIds(categoryId);
            expandedIds.addAll(descendantIds);
        }
        
        // 去重
        return expandedIds.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 转换为VO
     * 
     * @param transaction 交易实体
     * @return 交易VO
     */
    private TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);
        return vo;
    }
}
