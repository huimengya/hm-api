package com.qingshu.hmapisdk.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.qingshu.hmapisdk.client.HmApiClient;
import com.qingshu.hmapisdk.exception.ApiException;
import com.qingshu.hmapisdk.exception.ErrorCode;
import com.qingshu.hmapisdk.exception.ErrorResponse;
import com.qingshu.hmapisdk.model.request.BaseRequest;
import com.qingshu.hmapisdk.model.response.ResultResponse;
import com.qingshu.hmapisdk.utils.SignUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Slf4j
@Data
public abstract class BaseService implements ApiService {
    private HmApiClient hmApiClient;
    /**
     * 网关HOST
     */
    // 线上
    // 如果加上/api,对于get请求，拼接参数时会出现问题，例如：http://localhost:8090/api/api/name?name=123
    // 因为数据库存的：/api/name
    private String gatewayHost = "http://www.baidu.com:4760";

    /**
     * 检查配置
     *
     * @param hmApiClient hm api客户端
     * @throws ApiException 业务异常
     */
    public void checkConfig(HmApiClient hmApiClient) throws ApiException {
        if (hmApiClient == null && this.getHmApiClient() == null) {
            throw new ApiException(ErrorCode.NO_AUTH_ERROR, "请先配置密钥AccessKey/SecretKey");
        }
        if (hmApiClient != null && !StringUtils.isAnyBlank(hmApiClient.getAccessKey(), hmApiClient.getSecretKey())) {
            this.setHmApiClient(hmApiClient);
        }
    }

    /**
     * 执行请求
     *
     * @param request 请求
     * @return {@link HttpResponse}
     * @throws ApiException 业务异常
     */
    private <O, T extends ResultResponse> HttpResponse doRequest(BaseRequest<O, T> request) throws ApiException {
        try (HttpResponse httpResponse = getHttpRequestByRequestMethod(request).execute()) {
            return httpResponse;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    /**
     * 通过请求方法获取http响应
     *
     * @param request 要求
     * @return {@link HttpResponse}
     * @throws ApiException 业务异常
     */
    private <O, T extends ResultResponse> HttpRequest getHttpRequestByRequestMethod(BaseRequest<O, T> request) throws ApiException {
        if (ObjectUtils.isEmpty(request)) {
            throw new ApiException(ErrorCode.OPERATION_ERROR, "请求参数错误");
        }
        // 获取请求方法和请求路径
        String path = request.getPath().trim();
        String method = request.getMethod().trim().toUpperCase();

        if (ObjectUtils.isEmpty(method)) {
            throw new ApiException(ErrorCode.OPERATION_ERROR, "请求方法不存在");
        }
        if (StringUtils.isBlank(path)) {
            throw new ApiException(ErrorCode.OPERATION_ERROR, "请求路径不存在");
        }
        // 如果path以gatewayHost开头，去掉path开头的gatewayHost
        // 例如：https://localhost:8090/api/name -> /api/name
        if (path.startsWith(gatewayHost)) {
            // 去掉path开头的gatewayHost 例如：https://localhost:8090/api/name -> /api/name
            path = path.substring(gatewayHost.length());
        }
        log.info("请求方法：{}，请求路径：{}，请求参数：{}", method, path, request.getRequestParams());
        HttpRequest httpRequest;
        switch (method) {
            case "GET": {
                // 拼接Get请求 例如：http://localhost:8090/api/name?name=123   name=123是请求参数
                String s = splicingGetRequest(request, path);
                httpRequest = HttpRequest.get(s);
                break;
            }
            case "POST": {
                // post请求 例如：http://localhost:8090/api/name
                httpRequest = HttpRequest.post(gatewayHost + path);
                break;
            }
            default: {
                throw new ApiException(ErrorCode.OPERATION_ERROR, "不支持该请求");
            }
        }
        return httpRequest.addHeaders(getHeaders(JSONUtil.toJsonStr(request), hmApiClient)).body(JSONUtil.toJsonStr(request.getRequestParams()));
    }

    /**
     * 获取响应数据
     *
     * @param request 要求
     * @return {@link T}
     * @throws ApiException 业务异常
     */
    public <O, T extends ResultResponse> T res(BaseRequest<O, T> request) throws ApiException {
        Class<T> clazz = null; // 在 try 块之外初始化 clazz
        if (hmApiClient == null || StringUtils.isAnyBlank(hmApiClient.getAccessKey(), hmApiClient.getSecretKey())) {
            throw new ApiException(ErrorCode.NO_AUTH_ERROR, "请先配置密钥AccessKey/SecretKey");
        }
        T rsp;
        try {
            clazz = request.getResponseClass();
            rsp = clazz.newInstance();
        } catch (Exception e) {
            throw new ApiException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
        // 执行请求：http://localhost:8090/api/name?name=123
        HttpResponse httpResponse = doRequest(request);
        String body = httpResponse.body();
        Map<String, Object> data = new HashMap<>();
        if (httpResponse.getStatus() != 200) {
            ErrorResponse errorResponse = JSONUtil.toBean(body, ErrorResponse.class);
            data.put("errorMessage", errorResponse.getMessage());
            data.put("code", errorResponse.getCode());
        } else {
            try {
                // 尝试解析为JSON对象
                data = new Gson().fromJson(body, new TypeToken<Map<String, Object>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                // 解析失败，将body作为普通字符串处理
                data.put("value", body);
            }
        }
        rsp.setData(data);
        return rsp;
    }

    /**
     * 拼接Get请求
     *
     * @param request 要求
     * @param path    路径
     * @return {@link String}
     */
    private <O, T extends ResultResponse> String splicingGetRequest(BaseRequest<O, T> request, String path) {
        StringBuilder urlBuilder = new StringBuilder(gatewayHost);
        // urlBuilder最后是/结尾且path以/开头的情况下，去掉urlBuilder结尾的/
        if (urlBuilder.toString().endsWith("/") && path.startsWith("/")) {
            urlBuilder.setLength(urlBuilder.length() - 1);
        }
        urlBuilder.append(path);
        if (!request.getRequestParams().isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, Object> entry : request.getRequestParams().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                urlBuilder.append(key).append("=").append(value).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        log.info("GET请求路径：{}", urlBuilder);
        return urlBuilder.toString();
    }


    /**
     * 获取请求头
     *
     * @param body        请求体
     * @param hmApiClient hm api客户端
     * @return {@link Map}<{@link String}, {@link String}>
     */
    private Map<String, String> getHeaders(String body, HmApiClient hmApiClient) {
        Map<String, String> hashMap = new HashMap<>(4);
        hashMap.put("accessKey", hmApiClient.getAccessKey());
        String encodedBody = SecureUtil.md5(body);
        hashMap.put("body", encodedBody);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashMap.put("sign", SignUtils.getSign(encodedBody, hmApiClient.getSecretKey()));
        return hashMap;
    }

    @Override
    public <O, T extends ResultResponse> T request(BaseRequest<O, T> request) throws ApiException {
        try {
            return res(request);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public <O, T extends ResultResponse> T request(HmApiClient hmApiClient, BaseRequest<O, T> request) throws ApiException {
        checkConfig(hmApiClient);
        return request(request);
    }
}
