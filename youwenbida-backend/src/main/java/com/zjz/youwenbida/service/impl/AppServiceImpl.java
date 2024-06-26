package com.zjz.youwenbida.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.exception.ThrowUtils;
import com.zjz.youwenbida.mapper.AppMapper;
import com.zjz.youwenbida.model.dto.app.AppQueryRequest;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.User;
import com.zjz.youwenbida.model.vo.AppVO;
import com.zjz.youwenbida.model.vo.UserVO;
import com.zjz.youwenbida.service.AppService;
import com.zjz.youwenbida.service.UserService;
import com.zjz.youwenbida.utils.ValidEnumUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 *
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param app
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        String appIcon = app.getAppIcon();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR,"应用标题不能为空");
            // 枚举类校验
            ThrowUtils.throwIf(ValidEnumUtils.validAppTypeEnum(appType), ErrorCode.PARAMS_ERROR, "应用类型不能为空或非法");
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 128, ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(appDesc)) {
            ThrowUtils.throwIf(appDesc.length() > 256, ErrorCode.PARAMS_ERROR, "描述过长");
        }
        if (StringUtils.isNotBlank(appIcon)) {
            ThrowUtils.throwIf(appIcon.length() > 1024, ErrorCode.PARAMS_ERROR, "图标过长");
        }
        if (ObjectUtils.isEmpty(appType)) {
            ThrowUtils.throwIf(ValidEnumUtils.validAppTypeEnum(appType),
                    ErrorCode.PARAMS_ERROR, "应用类型不能为空或非法");
        }
        if (ObjectUtils.isEmpty(scoringStrategy)) {
            ThrowUtils.throwIf(ValidEnumUtils.validScoringStrategyEnum(scoringStrategy),
                    ErrorCode.PARAMS_ERROR, "评分策略不能为空或非法");
        }
    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();  // 模糊查询
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        Long userId = appQueryRequest.getUserId();
        String searchText = appQueryRequest.getSearchText();

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("appDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(appName),"appName",appName);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType),"appType",appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy),"scoringStrategy",scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus),"reviewStatus",reviewStatus);
        return queryWrapper;
    }

    /**
     * 获取应用封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);
        // 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);
        return appVO;
    }

    /**
     * 分页获取应用封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> appVOList = appList.stream().map(AppVO::objToVo).collect(Collectors.toList());

        // 关联查询用户信息
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        appVOList.forEach(appVO -> {
            Long userId = appVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
        });
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

}
