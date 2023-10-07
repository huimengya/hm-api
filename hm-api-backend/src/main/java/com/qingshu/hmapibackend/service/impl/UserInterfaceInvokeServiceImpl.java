package com.qingshu.hmapibackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingshu.hmapibackend.common.ErrorCode;
import com.qingshu.hmapibackend.exception.BusinessException;
import com.qingshu.hmapibackend.mapper.UserInterfaceInvokeMapper;
import com.qingshu.hmapibackend.model.vo.InterfaceInvokeCountVO;
import com.qingshu.hmapibackend.service.InterfaceInfoService;
import com.qingshu.hmapibackend.service.UserInterfaceInvokeService;
import com.qingshu.hmapicommon.model.entity.InterfaceInfo;
import com.qingshu.hmapicommon.model.entity.UserInterfaceInvoke;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author qingshu
* @description 针对表【user_interface_invoke(用户接口调用表)】的数据库操作Service实现
* @createDate 2023-10-03 20:12:29
*/
@Service
public class UserInterfaceInvokeServiceImpl extends ServiceImpl<UserInterfaceInvokeMapper, UserInterfaceInvoke>
    implements UserInterfaceInvokeService {

    @Resource
    private UserInterfaceInvokeMapper userInterfaceInvokeMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public List<InterfaceInvokeCountVO> getTotalInvokes(Long limit) {
        // 1、获取接口调用总调用次数TOP5
        QueryWrapper<UserInterfaceInvoke> wrapper = new QueryWrapper<>();
        wrapper.select("interfaceId", "SUM(totalInvokes) as totalInvokes") // 选择要查询的列并使用SUM函数
                .groupBy("interfaceId") // 使用GROUP BY子句
                .orderByDesc("totalInvokes") // 按总调用次数降序排序
                .last("LIMIT " + limit); // 限制结果数量
        return getInvokeCount(wrapper);
    }

    @Override
    public List<InterfaceInvokeCountVO> getTotalInvokesByUserId(Long userId, Long limit) {
        // 1、获取接口调用总调用次数TOP5 根据用户id
        QueryWrapper<UserInterfaceInvoke> wrapper = new QueryWrapper<>();
        wrapper.select("interfaceId","totalInvokes")
                .orderByDesc("totalInvokes")
                .last("LIMIT " + limit);
        // eq方法是等于的意思,相当于sql语句中的where条件
        wrapper.eq("userId",userId);
        return getInvokeCount(wrapper);
    }

    private List<InterfaceInvokeCountVO> getInvokeCount(QueryWrapper<UserInterfaceInvoke> queryWrapper){
        List<UserInterfaceInvoke> invokeList = userInterfaceInvokeMapper.selectList(queryWrapper);
        // 如果为空,用户还没有开始调用接口
        if (invokeList.isEmpty()){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"当前没有调用数据");
        }
        // 2、将invokeList集合中的接口id收集到idList中,这里map方法是将invokeList集合中的每一个元素都映射成一个新的元素，这里是将每一个元素的interfaceId映射成一个新的元素
        List<Long> idList = invokeList.stream().map(UserInterfaceInvoke::getInterfaceId).collect(Collectors.toList());
        // 2.1、根据idList查询接口信息 查询的sql语句类似于：select * from interface_info where id in (1,2,3,4,5)
        List<InterfaceInfo> interfaceInfos = interfaceInfoService.listByIds(idList);
        // 2.2、每个id对应的调用次数
        List<Long> totalInvokesList = invokeList.stream().map(UserInterfaceInvoke::getTotalInvokes).collect(Collectors.toList());
        // 2.3、将接口name收集到list中
        List<String> interfaceNameList = interfaceInfos.stream().map(InterfaceInfo::getName).collect(Collectors.toList());
        // 3、将接口名字和对应的调用次数封装到vo中，返回
        List<InterfaceInvokeCountVO> result = new ArrayList<>();
        for (int i = 0; i < interfaceNameList.size(); i++) {
            String interfaceName = interfaceNameList.get(i);
            Long totalInvokes = totalInvokesList.get(i);
            InterfaceInvokeCountVO vo = new InterfaceInvokeCountVO(interfaceName, totalInvokes);
            result.add(vo);
        }
        return result;
    }
}




