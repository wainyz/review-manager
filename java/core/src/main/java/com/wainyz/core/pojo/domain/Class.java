package com.wainyz.core.pojo.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.wainyz.core.CoreApplication;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* 
* @author Yanion_gwgzh
 * &#064;TableName  class
 */
@Data
public class Class implements Serializable {

    private String id;

    private Long owner;

    private String teacherList;

    private String className;

    private String description;

    private String examList;

    private String studentList;

    private int isPublic;

    public static Logger LOGGER = LoggerFactory.getLogger(Class.class);
    public static List<Long> convertToList(String string){
        try{
            JsonNode jsonNode = CoreApplication.OBJECT_MAPPER.readTree(string);
            ArrayList<Long> result = new ArrayList<>();
            for ( JsonNode jsonNode1 : jsonNode){
                 result.add(jsonNode1.longValue());
            }
            return result;
        } catch (JsonProcessingException e) {
            LOGGER.error( "[47] json 转换失败");
            return new ArrayList<>();
        }
    }
}
