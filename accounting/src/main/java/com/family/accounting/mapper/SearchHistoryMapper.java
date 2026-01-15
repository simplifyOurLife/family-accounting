package com.family.accounting.mapper;

import com.family.accounting.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索历史数据访问接口
 */
@Mapper
public interface SearchHistoryMapper {
    
    /**
     * 插入搜索历史记录
     * 
     * @param searchHistory 搜索历史对象
     * @return 影响行数
     */
    int insert(SearchHistory searchHistory);
    
    /**
     * 根据用户ID、家庭ID和关键词查询搜索历史
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @param keyword 关键词
     * @return 搜索历史对象
     */
    SearchHistory findByUserFamilyAndKeyword(@Param("userId") Long userId, 
                                             @Param("familyId") Long familyId, 
                                             @Param("keyword") String keyword);
    
    /**
     * 更新搜索次数
     * 
     * @param id 搜索历史ID
     * @return 影响行数
     */
    int incrementSearchCount(@Param("id") Long id);
    
    /**
     * 获取用户的搜索建议（按搜索次数和更新时间排序）
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @param limit 返回数量限制
     * @return 搜索历史列表
     */
    List<SearchHistory> findSuggestions(@Param("userId") Long userId, 
                                        @Param("familyId") Long familyId, 
                                        @Param("limit") int limit);
    
    /**
     * 获取用户的搜索历史列表
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @return 搜索历史列表
     */
    List<SearchHistory> findByUserAndFamily(@Param("userId") Long userId, 
                                            @Param("familyId") Long familyId);
    
    /**
     * 删除指定的搜索历史记录
     * 
     * @param id 搜索历史ID
     * @param userId 用户ID（用于权限验证）
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 清空用户的所有搜索历史
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @return 影响行数
     */
    int deleteByUserAndFamily(@Param("userId") Long userId, 
                              @Param("familyId") Long familyId);
}
