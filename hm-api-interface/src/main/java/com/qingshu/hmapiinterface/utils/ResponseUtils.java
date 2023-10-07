package com.qingshu.hmapiinterface.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qingshu.hmapisdk.exception.ApiException;
import com.qingshu.hmapisdk.model.response.ResultResponse;

import java.util.Map;

import static com.qingshu.hmapiinterface.utils.RequestUtils.get;


/**
 * @Author: qingshu
 * @Date: 2023年09月22日 17:18
 * @Version: 1.0
 * @Description:
 */
public class ResponseUtils {
    public static Map<String, Object> responseToMap(String response) {
        return new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    public static <T> ResultResponse baseResponse(String baseUrl, T params) {
        String response = null;
        try {
            response = get(baseUrl, params);
            Map<String, Object> fromResponse = responseToMap(response);
            boolean success = (boolean) fromResponse.get("success");
            ResultResponse baseResponse = new ResultResponse();
            if (!success) {
                baseResponse.setData(fromResponse);
                return baseResponse;
            }
            fromResponse.remove("success");
            baseResponse.setData(fromResponse);
            return baseResponse;
        } catch (ApiException e) {
            throw new RuntimeException("构建URL失败：",e);
        }
    }
}
