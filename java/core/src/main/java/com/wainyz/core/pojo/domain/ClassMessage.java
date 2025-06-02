package com.wainyz.core.pojo.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yanion_gwgzh
 */
@Data
public class ClassMessage implements Serializable {

    private String id;

    private Long classId;

    private Date endtime;

    private Object content;

    private Date starttime;

}
