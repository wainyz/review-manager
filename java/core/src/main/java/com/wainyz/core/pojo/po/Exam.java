package com.wainyz.core.pojo.po;

import lombok.Data;

import java.io.Serializable;

/**
* 
* @author Yanion_gwgzh
 * @TableName exam
*/
@Data
public class Exam implements Serializable {
    private Long id;
    private Long ownerId;
    private String title;
    private String description;
    private Object content;
    private Object students;
    private Object teachers;
}
