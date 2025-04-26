package com.wainyz.commons.pojo.domin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Yanion_gwgzh
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ImageCodeDO {
    @JsonProperty("image_id")
    public String imageId;
    @JsonProperty("image_code")
    public String imageCode;
}
