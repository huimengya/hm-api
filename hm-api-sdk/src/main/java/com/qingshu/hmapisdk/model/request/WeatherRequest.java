package com.qingshu.hmapisdk.model.request;

import com.qingshu.hmapisdk.model.enums.RequestMethodEnum;
import com.qingshu.hmapisdk.model.params.WeatherParams;
import com.qingshu.hmapisdk.model.response.ResultResponse;
import lombok.experimental.Accessors;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Accessors(chain = true)
public class WeatherRequest extends BaseRequest<WeatherParams, ResultResponse> {

    @Override
    public String getPath() {
        return "/weather";
    }

    /**
     * 获取响应类
     *
     * @return {@link Class}<{@link NameResponse}>
     */
    @Override
    public Class<ResultResponse> getResponseClass() {
        return ResultResponse.class;
    }


    @Override
    public String getMethod() {
        return RequestMethodEnum.GET.getValue();
    }
}
