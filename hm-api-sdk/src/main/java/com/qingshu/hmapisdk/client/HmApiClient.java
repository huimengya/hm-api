package com.qingshu.hmapisdk.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HmApiClient {
    /**
     * 访问密钥
     */
    private String accessKey ;

    /**
     * 密钥
     */
    private String secretKey;
}
