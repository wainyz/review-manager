package com.wainyz.core.consident;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author ：wainyz
 * &#064;date  ：Created in 2025/4/2
 * &#064;description：用于表示衰减类型
 */
@Getter
public enum AttenuationEnum {
    Leve0(0,0.01, Integer.MAX_VALUE),
    Leve1(1,0.05, 15),
    Leve2(2,0.1, 7),
    Leve3(3,0.2, 3),
    Leve4(4,0.5, 2),
    Leve5(5,0.8, 1);
    @JsonValue
    private final int level;
    private final double attenuation;
    private final int time;

    AttenuationEnum(int level, double attenuation, int time) {
        this.level = level;
        this.attenuation = attenuation;
        this.time = time;
    }
    public static AttenuationEnum getAttenuationEnum(int level) {
        if (level < 0){
            return Leve0;
        }
        for (AttenuationEnum value : AttenuationEnum.values()) {
            if (value.getLevel() == level) {
                return value;
            }
        }
        return getMaxAttenuationEnum();
    }
    public static AttenuationEnum getMaxAttenuationEnum() {
        AttenuationEnum max = Leve0;
        for (AttenuationEnum value : AttenuationEnum.values()) {
            if (value.getLevel() > max.getLevel()) {
                max = value;
            }
        }
        return max;
    }
}
