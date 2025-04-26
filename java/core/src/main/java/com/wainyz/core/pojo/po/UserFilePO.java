package com.wainyz.core.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

/**
 * @author Yanion_gwgzh
 */
@Data
@TableName("user_file")
@Builder
public class UserFilePO implements Serializable {
    @TableId
    private Long id;
    private String title;
    private String filePath;
    private Long owner;
    private int isPublic;
}
