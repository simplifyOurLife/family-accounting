package com.family.accounting.mapper;

import com.family.accounting.entity.SavedFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 保存的筛选条件数据访问接口
 */
@Mapper
public interface SavedFilterMapper {
    
    /**
     * 插入筛选条件
     * 
     * @param savedFilter 筛选条件对象
     * @return 影响行数
     */
    int insert(SavedFilter savedFilter);
    
    /**
     * 更新筛选条件
     * 
     * @param savedFilter 筛选条件对象
     * @return 影响行数
     */
    int update(SavedFilter savedFilter);
    
    /**
     * 根据ID查询筛选条件
     * 
     * @param id 筛选条件ID
     * @param userId 用户ID（用于权限验证）
     * @return 筛选条件对象
     */
    SavedFilter findById(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 获取用户的所有保存的筛选条件
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @return 筛选条件列表
     */
    List<SavedFilter> findByUserAndFamily(@Param("userId") Long userId, 
                                          @Param("familyId") Long familyId);
    
    /**
     * 删除筛选条件
     * 
     * @param id 筛选条件ID
     * @param userId 用户ID（用于权限验证）
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 根据名称查询筛选条件（用于检查重名）
     * 
     * @param userId 用户ID
     * @param familyId 家庭ID
     * @param name 筛选条件名称
     * @return 筛选条件对象
     */
    SavedFilter findByName(@Param("userId") Long userId, 
                          @Param("familyId") Long familyId, 
                          @Param("name") String name);
}
