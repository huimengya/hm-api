package com.qingshu.hmapisdk.model.params;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@Accessors(chain = true)
public class WeatherParams implements Serializable {
    private static final long serialVersionUID = -4649467322619145033L;
    private String ip;
    private String city;
    private String type;
}
