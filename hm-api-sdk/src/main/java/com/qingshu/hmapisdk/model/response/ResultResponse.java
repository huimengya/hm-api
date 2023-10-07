package com.qingshu.hmapisdk.model.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@NoArgsConstructor
public class ResultResponse implements Serializable {

    private static final long serialVersionUID = 5333596094882935453L;
    private Map<String, Object> data = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}