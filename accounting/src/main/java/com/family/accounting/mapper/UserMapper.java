package com.family.accounting.mapper;

import com.family.accounting.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体
     */
    User findById(@Param("id") Long id);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 插入新用户
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int update(User user);

    /**
     * 更新用户密码
     *
     * @param id       用户ID
     * @param password 新密码(已加密)
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 存在返回1，不存在返回0
     */
    int existsByPhone(@Param("phone") String phone);
}
