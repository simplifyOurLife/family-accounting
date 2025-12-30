package com.family.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 家庭创建/更新请求DTO
 */
@Data
public class FamilyDTO {

    /**
     * 家庭名称
     */
    @NotBlank(message = "家庭名称不能为空")
    @Size(max = 50, message = "家庭名称长度不能超过50个字符")
    private String name;
}
