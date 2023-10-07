package com.qingshu.hmapisdk.config;

import com.qingshu.hmapisdk.client.HmApiClient;
import com.qingshu.hmapisdk.service.ApiService;
import com.qingshu.hmapisdk.service.impl.ApiServiceImpl;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@Configuration
@ConfigurationProperties("hm.api.client")
@ComponentScan
public class HmApiClientConfig {
    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 网关
     */
    private String host;
    @Bean
    public HmApiClient hmApiClient() {
        return new HmApiClient(accessKey, secretKey);
    }
    @Bean
    public ApiService apiService() {
        ApiServiceImpl apiService = new ApiServiceImpl();
        apiService.setHmApiClient(new HmApiClient(accessKey, secretKey));
        if (StringUtils.isNotBlank(host)) {
            apiService.setGatewayHost(host);
        }
        return apiService;
    }
}
