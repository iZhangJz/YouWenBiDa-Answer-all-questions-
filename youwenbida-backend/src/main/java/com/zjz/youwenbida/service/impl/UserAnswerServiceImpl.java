package com.zjz.youwenbida.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.exception.ThrowUtils;
import com.zjz.youwenbida.mapper.UserAnswerMapper;
import com.zjz.youwenbida.model.dto.userAnswer.UserAnswerQueryRequest;
import com.zjz.youwenbida.model.entity.User;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.zjz.youwenbida.model.vo.UserAnswerVO;
import com.zjz.youwenbida.model.vo.UserVO;
import com.zjz.youwenbida.service.UserAnswerService;
import com.zjz.youwenbida.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户答题表服务实现
 *
 */
@Service
@Slf4j
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param userAnswer
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {
        ThrowUtils.throwIf(userAnswer == null, ErrorCode.PARAMS_ERROR);
        Long appId = userAnswer.getAppId();
        Long resultId = userAnswer.getResultId();
        String resultName = userAnswer.getResultName();
        String resultPicture = userAnswer.getResultPicture();
        Integer resultScore = userAnswer.getResultScore();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(ObjectUtils.isEmpty(appId),
                    ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        }
        if (ObjectUtils.isNotEmpty(resultId)) {
            ThrowUtils.throwIf(resultId < 0,
                    ErrorCode.PARAMS_ERROR, "结果ID不存在");
        }
        if(StringUtils.isNotBlank(resultName)){
            ThrowUtils.throwIf(resultName.length() > 128,
                    ErrorCode.PARAMS_ERROR, "结果名称长度超过限制");
        }
        if(StringUtils.isNotBlank(resultPicture)){
            ThrowUtils.throwIf(resultPicture.length() > 1024,
                    ErrorCode.PARAMS_ERROR, "结果图片长度超过限制");
        }
        if (ObjectUtils.isNotEmpty(resultScore)) {
            ThrowUtils.throwIf(resultScore < 0 || resultScore > 100,
                    ErrorCode.PARAMS_ERROR, "结果分数非法");
        }
    }

    /**
     * 获取查询条件
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryRequest == null) {
            return queryWrapper;
        }
        Long id = userAnswerQueryRequest.getId();
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String choices = userAnswerQueryRequest.getChoices();
        String resultName = userAnswerQueryRequest.getResultName();
        Integer resultScore = userAnswerQueryRequest.getResultScore();
        Long userId = userAnswerQueryRequest.getUserId();
        Date createTime = userAnswerQueryRequest.getCreateTime();
        String searchText = userAnswerQueryRequest.getSearchText();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("choices", searchText).or().like("resultName", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScore), "resultScore", resultScore);
        queryWrapper.ge(ObjectUtils.isNotEmpty(createTime), "createTime", createTime);
        return queryWrapper;
    }

    /**
     * 获取用户答题表封装
     *
     * @param userAnswer
     * @param request
     * @return
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(userAnswer);
        // 关联查询用户信息
        Long userId = userAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userAnswerVO.setUser(userVO);
        return userAnswerVO;
    }

    /**
     * 分页获取用户答题表封装
     *
     * @param userAnswerPage
     * @param request
     * @return
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> userAnswerPage, HttpServletRequest request) {
        List<UserAnswer> userAnswerList = userAnswerPage.getRecords();
        Page<UserAnswerVO> userAnswerVOPage =
                new Page<>(userAnswerPage.getCurrent(), userAnswerPage.getSize(), userAnswerPage.getTotal());
        if (CollUtil.isEmpty(userAnswerList)) {
            return userAnswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> userAnswerVOList =
                userAnswerList.stream().map(UserAnswerVO::objToVo).collect(Collectors.toList());

        //  关联查询用户信息
        Set<Long> userIdSet = userAnswerList.stream().map(UserAnswer::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        userAnswerVOList.forEach(userAnswerVO -> {
            Long userId = userAnswerVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            userAnswerVO.setUser(userService.getUserVO(user));
        });

        userAnswerVOPage.setRecords(userAnswerVOList);
        return userAnswerVOPage;
    }

}
