package com.qingshu.hmapisdk.service.impl;

import com.qingshu.hmapisdk.client.HmApiClient;
import com.qingshu.hmapisdk.exception.ApiException;
import com.qingshu.hmapisdk.model.request.*;
import com.qingshu.hmapisdk.model.response.LoveResponse;
import com.qingshu.hmapisdk.model.response.PoisonousChickenSoupResponse;
import com.qingshu.hmapisdk.model.response.RandomWallpaperResponse;
import com.qingshu.hmapisdk.model.response.ResultResponse;
import com.qingshu.hmapisdk.service.ApiService;
import com.qingshu.hmapisdk.service.BaseService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Slf4j
public class ApiServiceImpl extends BaseService implements ApiService {
    @Override
    public PoisonousChickenSoupResponse getPoisonousChickenSoup() throws ApiException {
        PoisonousChickenSoupRequest request = new PoisonousChickenSoupRequest();
        return request(request);
    }

    @Override
    public PoisonousChickenSoupResponse getPoisonousChickenSoup(HmApiClient hmApiClient) throws ApiException {
        PoisonousChickenSoupRequest request = new PoisonousChickenSoupRequest();
        return request(hmApiClient, request);
    }

    @Override
    public RandomWallpaperResponse getRandomWallpaper(RandomWallpaperRequest request) throws ApiException {
        return request(request);
    }

    @Override
    public RandomWallpaperResponse getRandomWallpaper(HmApiClient hmApiClient, RandomWallpaperRequest request) throws ApiException {
        return request(hmApiClient, request);
    }

    @Override
    public LoveResponse randomLoveTalk() throws ApiException {
        LoveRequest request = new LoveRequest();
        return request(request);
    }

    @Override
    public LoveResponse randomLoveTalk(HmApiClient hmApiClient) throws ApiException {
        LoveRequest request = new LoveRequest();
        return request(hmApiClient, request);
    }

    @Override
    public ResultResponse horoscope(HoroscopeRequest request) throws ApiException {
        return request(request);
    }

    @Override
    public ResultResponse horoscope(HmApiClient hmApiClient, HoroscopeRequest request) throws ApiException {
        return request(hmApiClient, request);
    }

    @Override
    public ResultResponse getIpInfo(HmApiClient hmApiClient, IpInfoRequest request) throws ApiException {
        return request(hmApiClient, request);
    }

    @Override
    public ResultResponse getIpInfo(IpInfoRequest request) throws ApiException {
        return request(request);
    }

    @Override
    public ResultResponse getWeatherInfo(HmApiClient hmApiClient, WeatherRequest request) throws ApiException {
        return request(hmApiClient, request);
    }

    @Override
    public ResultResponse getWeatherInfo(WeatherRequest request) throws ApiException {
        return request(request);
    }
}
