package com.family.accounting.mapper;

import com.family.accounting.entity.Captcha;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 图片验证码数据访问接口
 */
@Mapper
public interface CaptchaMapper {

    /**
     * 根据captchaKey查询验证码
     *
     * @param captchaKey 验证码唯一标识
     * @return 验证码实体
     */
    Captcha findByCaptchaKey(@Param("captchaKey") String captchaKey);

    /**
     * 插入新验证码
     *
     * @param captcha 验证码实体
     * @return 影响行数
     */
    int insert(Captcha captcha);

    /**
     * 根据captchaKey删除验证码
     *
     * @param captchaKey 验证码唯一标识
     * @return 影响行数
     */
    int deleteByCaptchaKey(@Param("captchaKey") String captchaKey);

    /**
     * 删除过期的验证码
     *
     * @param expiredBefore 过期时间阈值
     * @return 删除的记录数
     */
    int deleteExpired(@Param("expiredBefore") LocalDateTime expiredBefore);
}
