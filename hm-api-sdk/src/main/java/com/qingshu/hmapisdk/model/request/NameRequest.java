package com.qingshu.hmapisdk.model.request;

import com.qingshu.hmapisdk.model.enums.RequestMethodEnum;
import com.qingshu.hmapisdk.model.params.NameParams;
import com.qingshu.hmapisdk.model.response.NameResponse;
import lombok.experimental.Accessors;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Accessors(chain = true)
public class NameRequest extends BaseRequest<NameParams, NameResponse> {

    @Override
    public String getPath() {
        return "/name";
    }

    /**
     * 获取响应类
     *
     * @return {@link Class}<{@link NameResponse}>
     */
    @Override
    public Class<NameResponse> getResponseClass() {
        return NameResponse.class;
    }


    @Override
    public String getMethod() {
        return RequestMethodEnum.GET.getValue();
    }
}