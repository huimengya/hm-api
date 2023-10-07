package com.qingshu.hmapisdk.model.request;

import com.qingshu.hmapisdk.model.enums.RequestMethodEnum;
import com.qingshu.hmapisdk.model.response.LoveResponse;
import lombok.experimental.Accessors;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Accessors(chain = true)
public class LoveRequest extends BaseRequest<String, LoveResponse> {

    @Override
    public String getPath() {
        return "/loveTalk";
    }

    /**
     * 获取响应类
     *
     * @return {@link Class}<{@link NameResponse}>
     */
    @Override
    public Class<LoveResponse> getResponseClass() {
        return LoveResponse.class;
    }


    @Override
    public String getMethod() {
        return RequestMethodEnum.GET.getValue();
    }
}
