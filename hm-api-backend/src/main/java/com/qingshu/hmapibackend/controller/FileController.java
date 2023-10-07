package com.qingshu.hmapibackend.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qingshu.hmapibackend.common.BaseResponse;
import com.qingshu.hmapibackend.common.ErrorCode;
import com.qingshu.hmapibackend.common.ResultUtils;
import com.qingshu.hmapibackend.constant.FileConstant;
import com.qingshu.hmapibackend.manager.CosManager;
import com.qingshu.hmapibackend.model.entity.Advertisement;
import com.qingshu.hmapibackend.model.entity.User;
import com.qingshu.hmapibackend.model.enums.FileUploadBizEnum;
import com.qingshu.hmapibackend.model.enums.ImageStatusEnum;
import com.qingshu.hmapibackend.model.vo.ImageVo;
import com.qingshu.hmapibackend.model.vo.UserVO;
import com.qingshu.hmapibackend.service.AdvertisementService;
import com.qingshu.hmapibackend.service.InterfaceInfoService;
import com.qingshu.hmapibackend.service.UserService;
import com.qingshu.hmapicommon.model.entity.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

/**
 * 文件接口
 *
 * @author qingshu
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    final long ONE_M = 1024 * 1024*5L; // 5M
    @Resource
    private UserService userService;
    @Resource
    private CosManager cosManager; // 腾讯云cos

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private AdvertisementService advertisementService;


    /**
     * 上传文件
     *
     * @param multipartFile     多部分文件
     * @param request           请求
     * @return {@link BaseResponse}<{@link ImageVo}>
     */
    @PostMapping("/upload")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<ImageVo> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                            @RequestParam(value = "bizId") Long bizId,
                                            @RequestParam(value = "biz") String biz,
                                            HttpServletRequest request) {
        // 根据业务类型获取枚举
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        ImageVo imageVo = new ImageVo();
        if (fileUploadBizEnum == null) {
            return uploadError(imageVo, multipartFile, "上传失败,情重试.");
        }
        String result = validFile(multipartFile, fileUploadBizEnum);
        if (!"success".equals(result)) {
            return uploadError(imageVo, multipartFile, result);
        }
        UserVO loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;

        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            // 上传到腾讯云
            cosManager.putObject(filepath, file);
            imageVo.setName(multipartFile.getOriginalFilename());
            imageVo.setUid(RandomStringUtils.randomAlphanumeric(8));
            imageVo.setStatus(ImageStatusEnum.SUCCESS.getValue());
            // COS_HOST：访问地址 filepath：文件路径
            imageVo.setUrl(FileConstant.COS_HOST + filepath);
            // 将图片信息保存到数据库：根据业务来决定保存到哪张表
            if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
                // 保存用户头像
                User user = new User();
                user.setUserAvatar(filepath);
                UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                userUpdateWrapper.eq("id", bizId);
                boolean update = userService.update(user, userUpdateWrapper);
                if (!update) {
                    throw new RuntimeException("用户头像更新失败");
                }
            }
            if (FileUploadBizEnum.AD_AVATAR.equals(fileUploadBizEnum)) {
                // 保存广告图片，怎么拿到广告id  通过广告id去更新广告图片
                Advertisement advertisement = new Advertisement();
                advertisement.setAvatarUrl(filepath);
                UpdateWrapper<Advertisement> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", bizId);
                boolean update = advertisementService.update(advertisement, updateWrapper);
                if (!update) {
                    throw new RuntimeException("广告图片更新失败");
                }
            }
            if (FileUploadBizEnum.INTERFACE_AVATAR.equals(fileUploadBizEnum)) {
                // 保存接口头像
                InterfaceInfo interfaceInfo = new InterfaceInfo();
                interfaceInfo.setAvatarUrl(filepath);
                UpdateWrapper<InterfaceInfo> updateWrapper = new UpdateWrapper<>();
                // 执行的sql：update interface_info set avatar_url = ? where user_id = ?
                updateWrapper.eq("id", bizId);
                boolean update = interfaceInfoService.update(interfaceInfo, updateWrapper);
                if (!update) {
                    throw new RuntimeException("接口头像更新失败");
                }
            }
            // 返回可访问地址
            return ResultUtils.success(imageVo);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            return uploadError(imageVo, multipartFile, "上传失败,情重试");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    private BaseResponse<ImageVo> uploadError(ImageVo imageVo, MultipartFile multipartFile, String message) {
        imageVo.setName(multipartFile.getOriginalFilename());
        imageVo.setUid(RandomStringUtils.randomAlphanumeric(8));
        imageVo.setStatus(ImageStatusEnum.ERROR.getValue());
        return ResultUtils.error(imageVo, ErrorCode.OPERATION_ERROR, message);
    }

    /**
     * 有效文件
     * 校验文件
     *
     * @param fileUploadBizEnum 业务类型
     * @param multipartFile     多部分文件
     */
    private String validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 文件上传类型   用户头像
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                return "文件大小不能超过 5M";
            }
            // 校验文件类型 只能是图片 常见图片格式
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp","jiff","gif").contains(fileSuffix)) {
                return "文件类型错误";
            }
        }
        return "success";
    }
}
