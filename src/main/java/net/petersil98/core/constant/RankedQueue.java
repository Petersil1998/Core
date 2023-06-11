package net.petersil98.core.constant;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.petersil98.core.Core;

public enum RankedQueue {
    @JsonProperty("RANKED_SOLO_5x5")
    SOLO_DUO,
    @JsonProperty("RANKED_FLEX_SR")
    FLEX,
    @JsonProperty("RANKED_TFT")
    TFT,
    @JsonProperty("RANKED_TFT_DOUBLE_UP")
    DOUBLE_UP,
    @JsonProperty("RANKED_TFT_TURBO")
    HYPER_ROLL;

    public String getJsonPropertyValue() {
        try {
            return RankedQueue.class.getField(name()).getAnnotationsByType(JsonProperty.class)[0].value();
        } catch (NoSuchFieldException e) {
            Core.LOGGER.error("Couldn't get annotation value of RankedQueue type", e);
        }
        return name();
    }
}
