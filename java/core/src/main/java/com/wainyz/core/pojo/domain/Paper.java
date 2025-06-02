package com.wainyz.core.pojo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
* 
* @author Yanion_gwgzh
 * @TableName paper
*/
@Data
public class Paper implements Serializable {
    private String id;
    private String title;
    private Object content;
    @TableField("userId")
    private String userId;
}
