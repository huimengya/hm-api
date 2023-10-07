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
public class RandomWallpaperParams implements Serializable {
    private static final long serialVersionUID = 5948618236134980604L;
    private String lx;
    private String method;
}
