package com.family.accounting.service;

import com.family.accounting.dto.FilterConditionVO;
import com.family.accounting.dto.SavedFilterDTO;
import com.family.accounting.dto.SavedFilterVO;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.entity.SavedFilter;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.FamilyMemberMapper;
import com.family.accounting.mapper.SavedFilterMapper;
import com.family.accounting.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 筛选条件管理服务
 */
@Service
public class FilterService {
    
    private final SavedFilterMapper savedFilterMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ObjectMapper objectMapper;
    
    public FilterService(SavedFilterMapper savedFilterMapper, 
                        FamilyMemberMapper familyMemberMapper,
                        ObjectMapper objectMapper) {
        this.savedFilterMapper = savedFilterMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 保存筛选条件
     * 
     * @param dto 筛选条件DTO
     * @return 保存的筛选条件VO
     */
    @Transactional
    public SavedFilterVO saveFilter(SavedFilterDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        // 检查名称是否已存在
        SavedFilter existing = savedFilterMapper.findByName(userId, familyId, dto.getName());
        if (existing != null) {
            throw new BusinessException("筛选条件名称已存在");
        }
        
        // 将筛选条件转换为JSON
        String filterJson;
        try {
            filterJson = objectMapper.writeValueAsString(dto.getFilterCondition());
        } catch (JsonProcessingException e) {
            throw new BusinessException("筛选条件格式错误");
        }
        
        // 创建保存的筛选条件
        SavedFilter savedFilter = new SavedFilter();
        savedFilter.setUserId(userId);
        savedFilter.setFamilyId(familyId);
        savedFilter.setName(dto.getName());
        savedFilter.setFilterJson(filterJson);
        
        savedFilterMapper.insert(savedFilter);
        
        return convertToVO(savedFilter);
    }
    
    /**
     * 更新筛选条件
     * 
     * @param id 筛选条件ID
     * @param dto 筛选条件DTO
     * @return 更新后的筛选条件VO
     */
    @Transactional
    public SavedFilterVO updateFilter(Long id, SavedFilterDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        // 查询筛选条件
        SavedFilter savedFilter = savedFilterMapper.findById(id, userId);
        if (savedFilter == null) {
            throw new BusinessException("筛选条件不存在");
        }
        
        // 检查名称是否与其他筛选条件重复
        SavedFilter existing = savedFilterMapper.findByName(userId, familyId, dto.getName());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("筛选条件名称已存在");
        }
        
        // 将筛选条件转换为JSON
        String filterJson;
        try {
            filterJson = objectMapper.writeValueAsString(dto.getFilterCondition());
        } catch (JsonProcessingException e) {
            throw new BusinessException("筛选条件格式错误");
        }
        
        // 更新筛选条件
        savedFilter.setName(dto.getName());
        savedFilter.setFilterJson(filterJson);
        savedFilterMapper.update(savedFilter);
        
        return convertToVO(savedFilter);
    }
    
    /**
     * 获取所有保存的筛选条件
     * 
     * @return 筛选条件列表
     */
    public List<SavedFilterVO> getSavedFilters() {
        Long userId = SecurityUtils.getCurrentUserId();
        Long familyId = getCurrentFamilyId(userId);
        
        List<SavedFilter> savedFilters = savedFilterMapper.findByUserAndFamily(userId, familyId);
        return savedFilters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取筛选条件详情
     * 
     * @param id 筛选条件ID
     * @return 筛选条件VO
     */
    public SavedFilterVO getFilterById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        SavedFilter savedFilter = savedFilterMapper.findById(id, userId);
        if (savedFilter == null) {
            throw new BusinessException("筛选条件不存在");
        }
        
        return convertToVO(savedFilter);
    }
    
    /**
     * 应用筛选条件
     * 
     * @param id 筛选条件ID
     * @return 筛选条件
     */
    public FilterConditionVO applyFilter(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        SavedFilter savedFilter = savedFilterMapper.findById(id, userId);
        if (savedFilter == null) {
            throw new BusinessException("筛选条件不存在");
        }
        
        // 将JSON转换为筛选条件对象
        try {
            return objectMapper.readValue(savedFilter.getFilterJson(), FilterConditionVO.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("筛选条件格式错误");
        }
    }
    
    /**
     * 删除筛选条件
     * 
     * @param id 筛选条件ID
     */
    @Transactional
    public void deleteFilter(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        int rows = savedFilterMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("筛选条件不存在或无权删除");
        }
    }
    
    /**
     * 转换为VO
     * 
     * @param savedFilter 保存的筛选条件实体
     * @return 筛选条件VO
     */
    private SavedFilterVO convertToVO(SavedFilter savedFilter) {
        SavedFilterVO vo = new SavedFilterVO();
        vo.setId(savedFilter.getId());
        vo.setName(savedFilter.getName());
        vo.setCreatedAt(savedFilter.getCreatedAt());
        vo.setUpdatedAt(savedFilter.getUpdatedAt());
        
        // 将JSON转换为筛选条件对象
        try {
            FilterConditionVO filterCondition = objectMapper.readValue(
                    savedFilter.getFilterJson(), 
                    FilterConditionVO.class
            );
            vo.setFilterCondition(filterCondition);
        } catch (JsonProcessingException e) {
            // 如果解析失败，返回null
            vo.setFilterCondition(null);
        }
        
        return vo;
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
}
