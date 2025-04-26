package com.wainyz.commons.pojo.domin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Yanion_gwgzh
 */
@Data
public class LoginDO {
    public String email;
    public String password;
    @JsonProperty("image_code")
    public String imageCode;
    @JsonProperty("image_id")
    public String imageId;
}
