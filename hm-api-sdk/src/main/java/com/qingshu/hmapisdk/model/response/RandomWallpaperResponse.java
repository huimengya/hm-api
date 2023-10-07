package com.qingshu.hmapisdk.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RandomWallpaperResponse extends ResultResponse {
    private static final long serialVersionUID = -6443560279467567606L;
    private String imgurl;
}