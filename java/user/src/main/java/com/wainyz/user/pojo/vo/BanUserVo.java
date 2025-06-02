package com.wainyz.user.pojo.vo;

import lombok.Data;

/**
 * @author Yanion_gwgzh
 */
@Data
public class BanUserVo {
    String email;
    String username;
    Long userId;
    Long liftTime;
}
