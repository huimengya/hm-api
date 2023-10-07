package com.qingshu.hmapibackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.qingshu.hmapibackend.annotation.AuthCheck;
import com.qingshu.hmapibackend.common.*;
import com.qingshu.hmapibackend.constant.CommonConstant;
import com.qingshu.hmapibackend.constant.FileConstant;
import com.qingshu.hmapibackend.exception.BusinessException;
import com.qingshu.hmapibackend.model.dto.interfaceinfo.*;
import com.qingshu.hmapibackend.model.entity.User;
import com.qingshu.hmapibackend.model.enums.InterfaceStatusEnum;
import com.qingshu.hmapibackend.model.vo.UserVO;
import com.qingshu.hmapibackend.service.InterfaceInfoService;
import com.qingshu.hmapibackend.service.UserService;
import com.qingshu.hmapicommon.model.entity.InterfaceInfo;

import com.qingshu.hmapisdk.client.HmApiClient;
import com.qingshu.hmapisdk.model.request.CurrencyRequest;
import com.qingshu.hmapisdk.model.response.ResultResponse;
import com.qingshu.hmapisdk.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qingshu.hmapibackend.constant.UserConstant.ADMIN_ROLE;

/**
 * 帖子接口
 *
 * @author qingshu
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {
    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private UserService userService;
    @Resource
    private ApiService apiService;

    private final Gson gson = new Gson();

    // 定义地址常量
    private static final String URL = "http://localhost:8081";
    // region 增删改查

    /**
     * 添加接口信息
     * 创建
     *
     * @param interfaceInfoAddRequest 接口信息添加请求
     * @param request                 请求
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        if (CollectionUtils.isNotEmpty(interfaceInfoAddRequest.getRequestParams())) {
            List<RequestParamsField> requestParamsFields = interfaceInfoAddRequest.getRequestParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String requestParams = JSONUtil.toJsonStr(requestParamsFields);
            interfaceInfo.setRequestParams(requestParams);
        }
        if (CollectionUtils.isNotEmpty(interfaceInfoAddRequest.getResponseParams())) {
            List<ResponseParamsField> responseParamsFields = interfaceInfoAddRequest.getResponseParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            // 过滤出来fieldName字段
            String responseParams = JSONUtil.toJsonStr(responseParamsFields);
            interfaceInfo.setResponseParams(responseParams);
        }
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        UserVO loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除接口信息
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (ObjectUtils.anyNull(deleteRequest, deleteRequest.getId()) || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新接口头像url
     *
     * @param request                          请求
     * @param interfaceInfoUpdateAvatarRequest 界面信息更新头像请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/updateInterfaceInfoAvatar")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfoAvatarUrl(@RequestBody InterfaceInfoUpdateAvatarRequest interfaceInfoUpdateAvatarRequest,
                                                              HttpServletRequest request) {
        if (ObjectUtils.anyNull(interfaceInfoUpdateAvatarRequest, interfaceInfoUpdateAvatarRequest.getId()) || interfaceInfoUpdateAvatarRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateAvatarRequest, interfaceInfo);
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    /**
     * 更新接口信息
     *
     * @param interfaceInfoUpdateRequest 接口信息更新请求
     * @param request                    请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (ObjectUtils.anyNull(interfaceInfoUpdateRequest, interfaceInfoUpdateRequest.getId()) || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        if (CollectionUtils.isNotEmpty(interfaceInfoUpdateRequest.getRequestParams())) {
            List<RequestParamsField> requestParamsFields = interfaceInfoUpdateRequest.getRequestParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String requestParams = JSONUtil.toJsonStr(requestParamsFields);
            interfaceInfo.setRequestParams(requestParams);
        }
        if (CollectionUtils.isNotEmpty(interfaceInfoUpdateRequest.getResponseParams())) {
            List<ResponseParamsField> responseParamsFields = interfaceInfoUpdateRequest.getResponseParams().stream().filter(field -> StringUtils.isNotBlank(field.getFieldName())).collect(Collectors.toList());
            String responseParams = JSONUtil.toJsonStr(responseParamsFields);
            interfaceInfo.setResponseParams(responseParams);
        }
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        UserVO user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 前端传递的是完整的头像Url
        String avatarUrl = oldInterfaceInfo.getAvatarUrl();
        interfaceInfo.setAvatarUrl(avatarUrl);
        // 仅本人或管理员可修改
        if (!userService.isAdmin(request) && !oldInterfaceInfo.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 通过id获取接口信息
     *
     * @param id id
     * @return {@link BaseResponse}<{@link InterfaceInfo}>
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest 接口信息查询请求
     * @return {@link BaseResponse}<{@link List}<{@link InterfaceInfo}>>
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest 接口信息查询请求
     * @param request                   请求
     * @return {@link BaseResponse}<{@link Page}<{@link InterfaceInfo}>>
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String url = interfaceInfoQueryRequest.getUrl();

        String name = interfaceInfoQueryRequest.getName();
        long current = interfaceInfoQueryRequest.getCurrent();
        String method = interfaceInfoQueryRequest.getMethod();
        String description = interfaceInfoQueryRequest.getDescription();
        Integer status = interfaceInfoQueryRequest.getStatus();
        Integer reduceScore = interfaceInfoQueryRequest.getReduceScore();
        String returnFormat = interfaceInfoQueryRequest.getReturnFormat();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name)
                .like(StringUtils.isNotBlank(description), "description", description)
                .like(StringUtils.isNotBlank(url), "url", url)
                .like(StringUtils.isNotBlank(returnFormat), "returnFormat", returnFormat)
                .eq(StringUtils.isNotBlank(method), "method", method)
                .eq(ObjectUtils.isNotEmpty(status), "status", status)
                .eq(ObjectUtils.isNotEmpty(reduceScore), "reduceScore", reduceScore);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);

        // 不是管理员只能查看已经上线的
        // 判断是否是游客 游客只能查看已经上线的
        User user = userService.isTourist(request);
        if (user==null || !userService.isAdmin(request)) {
            // 拼接头像地址 首页展示才需要拼接
            interfaceInfoPage.getRecords().forEach(interfaceInfo -> {
                String avatarUrl = interfaceInfo.getAvatarUrl();
                if (StringUtils.isNotBlank(avatarUrl)) {
                    interfaceInfo.setAvatarUrl(FileConstant.COS_HOST + avatarUrl);
                }
            });
            List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords().stream()
                    .filter(interfaceInfo -> interfaceInfo.getStatus().equals(InterfaceStatusEnum.ONLINE.getValue())).collect(Collectors.toList());
            interfaceInfoPage.setRecords(interfaceInfoList);
        }
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 按搜索文本页查询数据
     *
     * @param interfaceInfoQueryRequest 接口信息查询请求
     * @param request                   请求
     * @return {@link BaseResponse}<{@link Page}<{@link InterfaceInfo}>>
     */
    @PostMapping("/get/searchText/")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoBySearchTextPage(@RequestBody InterfaceInfoSearchTextRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);

        String searchText = interfaceInfoQueryRequest.getSearchText();
        long size = interfaceInfoQueryRequest.getPageSize();
        long current = interfaceInfoQueryRequest.getCurrent();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        // 模糊查询 name 和 description
        queryWrapper.like(StringUtils.isNotBlank(searchText), "name", searchText)
                .or()
                .like(StringUtils.isNotBlank(searchText), "description", searchText);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);

        // 不是管理员只能查看已经上线的
        // 获取当前用户
        User user = userService.isTourist(request);
        if ( user==null || !userService.isAdmin(request)) {
            // 拼接头像地址 管理员不需要完整的头像地址，其他人都需要拼接
            interfaceInfoPage.getRecords().forEach(interfaceInfo -> {
                String avatarUrl = interfaceInfo.getAvatarUrl();
                if (StringUtils.isNotBlank(avatarUrl)) {
                    interfaceInfo.setAvatarUrl(FileConstant.COS_HOST + avatarUrl);
                }
            });
            List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords().stream()
                    .filter(interfaceInfo -> interfaceInfo.getStatus().equals(InterfaceStatusEnum.ONLINE.getValue())).collect(Collectors.toList());
            interfaceInfoPage.setRecords(interfaceInfoList);
        }
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 发布
     *
     * @param idRequest id请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/online")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (ObjectUtils.anyNull(idRequest, idRequest.getId()) || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        interfaceInfo.setStatus(InterfaceStatusEnum.ONLINE.getValue());
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    /**
     * 下线
     *
     * @param idRequest id请求
     * @param request   请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (ObjectUtils.anyNull(idRequest, idRequest.getId()) || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        interfaceInfo.setStatus(InterfaceStatusEnum.OFFLINE.getValue());
        return ResultUtils.success(interfaceInfoService.updateById(interfaceInfo));
    }

    // endregion

    /**
     * 调用接口
     *
     * @param invokeRequest id请求
     * @param request       请求
     * @return {@link BaseResponse}<{@link Object}>
     */
    @PostMapping("/invoke")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Object> invokeInterface(@RequestBody InvokeRequest invokeRequest, HttpServletRequest request) {
        Long id = invokeRequest.getId();
        List<InvokeRequest.Field> requestParams2 = invokeRequest.getRequestParams();
        String userRequestParams = invokeRequest.getUserRequestParams();
        if (ObjectUtils.anyNull(invokeRequest, invokeRequest.getId()) || invokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 0-审核中 1-开启 2-关闭
        if (interfaceInfo.getStatus() != InterfaceStatusEnum.ONLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口未开启");
        }
        // 构建请求参数
        List<InvokeRequest.Field> fieldList = invokeRequest.getRequestParams();
        String requestParams = "{}";
        if (fieldList != null && fieldList.size() > 0) {
            JsonObject jsonObject = new JsonObject();
            for (InvokeRequest.Field field : fieldList) {
                jsonObject.addProperty(field.getFieldName(), field.getValue());
            }
            requestParams = gson.toJson(jsonObject);
        }
        Map<String, Object> params = new Gson().fromJson(requestParams, new TypeToken<Map<String, Object>>() {
        }.getType());
        UserVO loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        Integer balance = loginUser.getBalance();
        if (balance <= 0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足，请先充值。");
        }
        try {
            HmApiClient hmApiClient = new HmApiClient(accessKey, secretKey);
            // 构建请求 通用请求
            CurrencyRequest currencyRequest = new CurrencyRequest();
            currencyRequest.setMethod(interfaceInfo.getMethod());
            currencyRequest.setPath(interfaceInfo.getUrl());
            currencyRequest.setRequestParams(params);
            ResultResponse response = apiService.request(hmApiClient, currencyRequest);
            return ResultUtils.success(response.getData());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
