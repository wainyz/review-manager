package com.wainyz.core.pojo.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 
* @author Yanion_gwgzh
 * @TableName friends_message
*/
@Data
public class FriendsMessage implements Serializable {


    private Long id;

    private Long friendId;

    private Date endtime;

    private Object content;

    private Date starttime;

}
