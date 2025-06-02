package com.wainyz.user.pojo.po;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author Yanion_gwgzh
 */
@Data
public class Ban {
    public Ban(){}
    public Ban(Long id, Timestamp liftTime){
        this.id = id;
        this.liftTime =liftTime ;
    }
    private Long id;
    private Timestamp liftTime;
}
