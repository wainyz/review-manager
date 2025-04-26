package com.wainyz.commons.pojo.domin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Yanion_gwgzh
 */
@Data
@Accessors(chain = true)
public class UserDO{
    public String username;
    @JsonProperty("user_id")
    public Long userId;
    public String email;
}
