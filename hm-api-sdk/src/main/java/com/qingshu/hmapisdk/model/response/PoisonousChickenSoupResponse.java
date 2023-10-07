package com.qingshu.hmapisdk.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PoisonousChickenSoupResponse extends ResultResponse {
    private static final long serialVersionUID = -6467312483425078539L;
    private String text;
}
