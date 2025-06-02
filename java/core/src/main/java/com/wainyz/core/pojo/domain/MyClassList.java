package com.wainyz.core.pojo.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.wainyz.core.CoreApplication;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* 
* @author Yanion_gwgzh
 * @TableName my_class_list
*/
@Data
@Accessors(chain = true)
@Component
public class MyClassList implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger( MyClassList.class);
    private Long id;
    private String classes;

    public List<Long> getClassesByList(){
        try {
            if (classes == null){
                return  new ArrayList<>();
            }
            JsonNode jsonNode = CoreApplication.OBJECT_MAPPER.readTree(classes);
            ArrayList<Long> longs = new ArrayList<>();
            for (JsonNode jsonNode1 : jsonNode) {
                longs.add(jsonNode1.longValue());
            }
            return longs;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public MyClassList setClassesByList(List<Long> classes){
        try {
            this.classes = CoreApplication.OBJECT_MAPPER.writeValueAsString(classes);
            return this;
        }catch ( JsonProcessingException e){
             logger.error( "[51]Class json 转换失败");
             return this;
        }
    }
}
