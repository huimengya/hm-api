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
public class IpInfoParams implements Serializable {
    private static final long serialVersionUID = 7861745746007047807L;
    private String ip;
}
