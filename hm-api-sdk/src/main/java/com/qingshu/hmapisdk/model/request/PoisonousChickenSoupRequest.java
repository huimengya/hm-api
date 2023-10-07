package com.qingshu.hmapisdk.model.request;

import com.qingshu.hmapisdk.model.enums.RequestMethodEnum;
import com.qingshu.hmapisdk.model.params.PoisonousChickenSoupParams;
import com.qingshu.hmapisdk.model.response.PoisonousChickenSoupResponse;
import lombok.experimental.Accessors;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Accessors(chain = true)
public class PoisonousChickenSoupRequest extends BaseRequest<PoisonousChickenSoupParams, PoisonousChickenSoupResponse> {

    @Override
    public String getPath() {
        return "/poisonousChickenSoup";
    }

    /**
     * 获取响应类
     *
     * @return {@link Class}<{@link PoisonousChickenSoupResponse}>
     */
    @Override
    public Class<PoisonousChickenSoupResponse> getResponseClass() {
        return PoisonousChickenSoupResponse.class;
    }

    @Override
    public String getMethod() {
        return RequestMethodEnum.GET.getValue();
    }
}