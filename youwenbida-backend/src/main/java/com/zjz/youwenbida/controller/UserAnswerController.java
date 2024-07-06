package com.zjz.youwenbida.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjz.youwenbida.annotation.AuthCheck;
import com.zjz.youwenbida.common.BaseResponse;
import com.zjz.youwenbida.common.DeleteRequest;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.common.ResultUtils;
import com.zjz.youwenbida.constant.UserConstant;
import com.zjz.youwenbida.exception.BusinessException;
import com.zjz.youwenbida.exception.ThrowUtils;
import com.zjz.youwenbida.model.dto.userAnswer.UserAnswerAddRequest;
import com.zjz.youwenbida.model.dto.userAnswer.UserAnswerEditRequest;
import com.zjz.youwenbida.model.dto.userAnswer.UserAnswerQueryRequest;
import com.zjz.youwenbida.model.dto.userAnswer.UserAnswerUpdateRequest;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.User;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.zjz.youwenbida.model.enums.ReviewStatusEnum;
import com.zjz.youwenbida.model.vo.UserAnswerVO;
import com.zjz.youwenbida.scoring.ScoringStrategyExecutor;
import com.zjz.youwenbida.service.AppService;
import com.zjz.youwenbida.service.UserAnswerService;
import com.zjz.youwenbida.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 用户答题表接口
 *
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    // region 增删改查

    /**
     * 创建用户答题表
     *
     * @param userAnswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(
            @RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userAnswerAddRequest == null, ErrorCode.PARAMS_ERROR);
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerAddRequest, userAnswer);
        String choices = JSONUtil.toJsonStr(userAnswerAddRequest.getChoices());
        userAnswer.setChoices(choices);
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, true);
        // 应用检查
        Long appId = userAnswerAddRequest.getAppId();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null,ErrorCode.NOT_FOUND_ERROR,"请求错误，未知应用");
        // 检查应用审核是否通过
        if (!Objects.equals(ReviewStatusEnum.getEnumByValue(app.getReviewStatus()), ReviewStatusEnum.PASS)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"应用审核未通过，无法答题");
        }

        User loginUser = userService.getLoginUser(request);
        userAnswer.setUserId(loginUser.getId());
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        // 写入数据库
        boolean result = userAnswerService.save(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,"系统错误");
        // 返回新写入的数据 id
        long newUserAnswerId = userAnswer.getId();

        // 计算评分结果
        UserAnswer userAnswerWithResult = scoringStrategyExecutor.doDispatch(userAnswerAddRequest.getChoices(), app);
        // 设置 id
        userAnswerWithResult.setId(newUserAnswerId);
        userAnswerWithResult.setAppId(null);
        boolean success = userAnswerService.updateById(userAnswerWithResult);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR,"系统错误");

        return ResultUtils.success(newUserAnswerId);
    }

    /**
     * 删除用户答题表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(
            @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答题表（仅管理员可用）
     *
     * @param userAnswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest) {
        if (userAnswerUpdateRequest == null || userAnswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerUpdateRequest, userAnswer);
        String choices = JSONUtil.toJsonStr(userAnswerUpdateRequest.getChoices());
        userAnswer.setChoices(choices);
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        // 判断是否存在
        long id = userAnswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userAnswerService.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答题表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer userAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(userAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVO(userAnswer, request));
    }

    /**
     * 分页获取用户答题表列表（仅管理员可用）
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(
            @RequestBody UserAnswerQueryRequest userAnswerQueryRequest) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        return ResultUtils.success(userAnswerPage);
    }

    /**
     * 分页获取用户答题表列表（封装类）
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(
            @RequestBody UserAnswerQueryRequest userAnswerQueryRequest, HttpServletRequest request) {
        return getUserAnswerVOPageResponse(userAnswerQueryRequest, request);
    }


    /**
     * 分页获取当前登录用户创建的用户答题表列表
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(
            @RequestBody UserAnswerQueryRequest userAnswerQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userAnswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        userAnswerQueryRequest.setUserId(loginUser.getId());
        return getUserAnswerVOPageResponse(userAnswerQueryRequest, request);
    }


    private BaseResponse<Page<UserAnswerVO>> getUserAnswerVOPageResponse(
            UserAnswerQueryRequest userAnswerQueryRequest, HttpServletRequest request) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> appPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(appPage, request));
    }

    /**
     * 编辑用户答题表（给用户使用）
     *
     * @param userAnswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(
            @RequestBody UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
        if (userAnswerEditRequest == null || userAnswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerEditRequest, userAnswer);
        String choices = JSONUtil.toJsonStr(userAnswerEditRequest.getChoices());
        userAnswer.setChoices(choices);
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = userAnswerEditRequest.getId();
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerService.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
