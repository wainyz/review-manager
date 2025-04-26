package com.wainyz.user.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Yanion_gwgzh
 */
@Data
@Accessors(chain = true)
@TableName("user")
public class UserPO {
    public String username;
    @TableId
    public Long userId;
    public String email;
    public String password;
}
