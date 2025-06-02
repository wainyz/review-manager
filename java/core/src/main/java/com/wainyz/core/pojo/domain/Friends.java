package com.wainyz.core.pojo.domain;

import lombok.Data;

import java.io.Serializable;

/**
* 
* @author Yanion_gwgzh
 * @TableName friends
*/
@Data
public class Friends implements Serializable {


    private Long id;
    /**
    * 发送好友请求的用户Id
    */

    private Long smallid;
    /**
    * 同意好友请求的用户Id
    */

    private Long bigid;
}
