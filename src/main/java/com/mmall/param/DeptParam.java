package com.mmall.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class DeptParam {
    private Integer id;

    @NotBlank(message = "部门名称不能为空")
    @Length(max = 15, min = 2,message = "部门名称的长度在2-15个字之间")
    private String name;

    private Integer parentId = 0;

    @NotNull(message = "展示顺序不能为空")
    private Integer seq;

    @Length(message = "备注的长度需要在150个字以内")
    private String remake;
}
