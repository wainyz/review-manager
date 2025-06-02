package com.wainyz.user.pojo.po;

import java.io.Serializable;


/**
* 
* @TableName permission_registry
*/
public class PermissionRegistry implements Serializable {
    public PermissionRegistry(){}
    public PermissionRegistry(String serviceName, int permission){
        this.id = serviceName;
        this.permission = permission;
    }

    /**
    * 使用url作为权限id
    */
//    @NotBlank(message="[使用url作为权限id]不能为空")
//    @Size(max= 128,message="编码长度不能超过128")
//    @ApiModelProperty("使用url作为权限id")
//    @Length(max= 128,message="编码长度不能超过128")
    private String id;
    /**
    * 
    */
//    @NotNull(message="[]不能为空")
//    @ApiModelProperty("")
    private Integer permission;

    /**
    * 使用url作为权限id
    */
    public void setId(String id){
    this.id = id;
    }

    /**
    * 
    */
    public void setPermission(Integer permission){
    this.permission = permission;
    }


    /**
    * 使用url作为权限id
    */
    public String getId(){
    return this.id;
    }

    /**
    * 
    */
    public Integer getPermission(){
    return this.permission;
    }

}
