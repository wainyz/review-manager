package com.wainyz.user.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Yanion_gwgzh
 */
@Data
@TableName("user_info")
public class UserInfoPO {
    @TableId("user_id")
    public Long userId;
    public String nickname;
    public Short gender;
    public String myClass;
}
