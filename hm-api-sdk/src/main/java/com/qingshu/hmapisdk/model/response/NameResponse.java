package com.qingshu.hmapisdk.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NameResponse extends ResultResponse {

    private static final long serialVersionUID = -3559839544118369820L;
    private String name;
}
