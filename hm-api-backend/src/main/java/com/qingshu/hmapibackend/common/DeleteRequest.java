package com.qingshu.hmapibackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author qingshu
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
}