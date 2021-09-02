package com.mszlu.blog.admin.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Permission {
    @TableId(type = IdType.AUTO)//自增，非分布式id
    private Long id;
    private String name;

    private String path;

    private String description;
}
