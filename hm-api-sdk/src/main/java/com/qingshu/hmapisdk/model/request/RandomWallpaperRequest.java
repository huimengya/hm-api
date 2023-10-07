package com.qingshu.hmapisdk.model.request;

import com.qingshu.hmapisdk.model.enums.RequestMethodEnum;
import com.qingshu.hmapisdk.model.params.RandomWallpaperParams;
import com.qingshu.hmapisdk.model.response.RandomWallpaperResponse;
import lombok.experimental.Accessors;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Accessors(chain = true)
public class RandomWallpaperRequest extends BaseRequest<RandomWallpaperParams, RandomWallpaperResponse> {
    @Override
    public String getPath() {
        return "/randomWallpaper";
    }

    /**
     * 获取响应类
     *
     * @return {@link Class}<{@link ResultResponse}>
     */
    @Override
    public Class<RandomWallpaperResponse> getResponseClass() {
        return RandomWallpaperResponse.class;
    }


    @Override
    public String getMethod() {
        return RequestMethodEnum.GET.getValue();
    }
}
