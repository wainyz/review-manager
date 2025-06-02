package com.wainyz.core.pojo.domain;

import cn.hutool.core.util.IdUtil;
import com.wainyz.core.consident.NoticeTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 
* @author Yanion_gwgzh
 * &#064;TableName  notice
 */
@Data
public class Notice implements Serializable {

    /**
    *
     * -- GETTER --
     *

     */
//    @NotNull(message="[]不能为空")
//    @ApiModelProperty("")
    private Long id;
    /**
    *
     * -- GETTER --
     *

     */
//    @NotNull(message="[]不能为空")
//    @ApiModelProperty("")
    private Long userid;
    /**
    *
     * -- GETTER --
     *

     */
//    @NotNull(message="[]不能为空")
//    @ApiModelProperty("")
    private Date timestamp;
    /**
    *
     * -- GETTER --
     *

     */
//    @NotBlank(message="[]不能为空")
//    @Size(max= 256,message="编码长度不能超过256")
//    @ApiModelProperty("")
//    @Length(max= 256,message="编码长度不能超过256")
    private String content;
    /**
    *
     * -- GETTER --
     *

     */
//    @NotNull(message="[]不能为空")
//    @ApiModelProperty("")
    private Integer type;

    public void setTypeByEnum(NoticeTypeEnum type){
        this.type = type.value;
    }
    public static Notice getTestObject(Long id){
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTimestamp(new Date());
        notice.setUserid(-1L);
        notice.setType(0);
        notice.setContent("test notice");
        return notice;
    }
    public static Notice build(Long userid, NoticeTypeEnum type, String...  params){
        Notice notice = new Notice();
        notice.setTimestamp(new Date());
        notice.setId(IdUtil.getSnowflake().nextId());
        notice.setUserid( userid);
        notice.setTypeByEnum(type);
        notice.setContent(type.stringify(params));
        return notice;
    }
}
