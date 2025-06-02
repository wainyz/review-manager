package com.wainyz.user.pojo.po;
import lombok.Data;

import java.io.Serializable;

/**
* 
* @author Yanion_gwgzh
 * &#064;TableName  user_permission
 */
@Data
public class UserPermission implements Serializable {
    public UserPermission(){}
    public UserPermission(Long userId, int permission){
        this.id = userId;
        this.permission = permission;
    }
    /**
    * 
    */
    private Long id;
    /**
    * 
    */
    private Integer permission;

    /**
    * 
    */
    public void setId(Long id){
    this.id = id;
    }

    /**
    * 
    */
    public void setPermission(Integer permission){
    this.permission = permission;
    }


    /**
    * 
    */
    public  Long getId(){
    return this.id;
    }

    /**
    * 
    */
    public Integer getPermission(){
    return this.permission;
    }

}
