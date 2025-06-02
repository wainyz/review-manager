package com.wainyz.core.pojo.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 
* @author Yanion_gwgzh
 * @TableName last_read
*/
@Data
public class LastRead implements Serializable {

    /**
    * 
    */


    private Long id;
    /**
    * 退出登录的时间。
    */

    private Date timestamp;


}
