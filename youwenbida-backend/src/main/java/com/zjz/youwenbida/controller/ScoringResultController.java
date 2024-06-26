package com.zjz.youwenbida.controller;

import cn.hutool.core.collection.ListUtil;
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
import com.zjz.youwenbida.model.dto.question.QuestionQueryRequest;
import com.zjz.youwenbida.model.dto.scoringResult.ScoringResultAddRequest;
import com.zjz.youwenbida.model.dto.scoringResult.ScoringResultEditRequest;
import com.zjz.youwenbida.model.dto.scoringResult.ScoringResultQueryRequest;
import com.zjz.youwenbida.model.dto.scoringResult.ScoringResultUpdateRequest;
import com.zjz.youwenbida.model.entity.Question;
import com.zjz.youwenbida.model.entity.ScoringResult;
import com.zjz.youwenbida.model.entity.User;
import com.zjz.youwenbida.model.vo.QuestionVO;
import com.zjz.youwenbida.model.vo.ScoringResultVO;
import com.zjz.youwenbida.service.ScoringResultService;
import com.zjz.youwenbida.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 结果分数表接口
 *
 */
@RestController
@RequestMapping("/scoringResult")
@Slf4j
public class ScoringResultController {

    @Resource
    private ScoringResultService scoringResultService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建结果分数表
     *
     * @param scoringResultAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addScoringResult(
            @RequestBody ScoringResultAddRequest scoringResultAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(scoringResultAddRequest == null, ErrorCode.PARAMS_ERROR);
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultAddRequest, scoringResult);
        String resultProp = JSONUtil.toJsonStr(scoringResultAddRequest.getResultProp());
        scoringResult.setResultProp(resultProp);
        // 数据校验
        scoringResultService.validScoringResult(scoringResult, true);
        User loginUser = userService.getLoginUser(request);
        scoringResult.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = scoringResultService.save(scoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newScoringResultId = scoringResult.getId();
        return ResultUtils.success(newScoringResultId);
    }

    /**
     * 删除结果分数表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteScoringResult(
            @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ScoringResult oldScoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldScoringResult.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = scoringResultService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新结果分数表（仅管理员可用）
     *
     * @param scoringResultUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateScoringResult(
            @RequestBody ScoringResultUpdateRequest scoringResultUpdateRequest) {
        if (scoringResultUpdateRequest == null || scoringResultUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultUpdateRequest, scoringResult);
        String resultProp = JSONUtil.toJsonStr(scoringResultUpdateRequest.getResultProp());
        scoringResult.setResultProp(resultProp);
        // 数据校验
        scoringResultService.validScoringResult(scoringResult, false);
        // 判断是否存在
        long id = scoringResultUpdateRequest.getId();
        ScoringResult oldScoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = scoringResultService.updateById(scoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取结果分数表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ScoringResultVO> getScoringResultVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        ScoringResult scoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(scoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(scoringResultService.getScoringResultVO(scoringResult, request));
    }

    /**
     * 分页获取结果分数表列表（仅管理员可用）
     *
     * @param scoringResultQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ScoringResult>> listScoringResultByPage(
            @RequestBody ScoringResultQueryRequest scoringResultQueryRequest) {
        long current = scoringResultQueryRequest.getCurrent();
        long size = scoringResultQueryRequest.getPageSize();
        // 查询数据库
        Page<ScoringResult> scoringResultPage = scoringResultService.page(new Page<>(current, size),
                scoringResultService.getQueryWrapper(scoringResultQueryRequest));
        return ResultUtils.success(scoringResultPage);
    }

    /**
     * 分页获取结果分数表列表（封装类）
     *
     * @param scoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listScoringResultVOByPage(
            @RequestBody ScoringResultQueryRequest scoringResultQueryRequest, HttpServletRequest request) {
        return getScoringResultVOPageResponse(scoringResultQueryRequest, request);
    }

    /**
     * 分页获取当前登录用户创建的结果分数表列表
     *
     * @param scoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listMyScoringResultVOByPage(
            @RequestBody ScoringResultQueryRequest scoringResultQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(scoringResultQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        scoringResultQueryRequest.setUserId(loginUser.getId());
        // 获取封装类
        return getScoringResultVOPageResponse(scoringResultQueryRequest, request);
    }


    private BaseResponse<Page<ScoringResultVO>> getScoringResultVOPageResponse(
            ScoringResultQueryRequest ScoringResultQueryRequest, HttpServletRequest request) {
        long current = ScoringResultQueryRequest.getCurrent();
        long size = ScoringResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<ScoringResult> questionPage = scoringResultService.page(new Page<>(current, size),
                scoringResultService.getQueryWrapper(ScoringResultQueryRequest));
        // 获取封装类
        return ResultUtils.success(scoringResultService.getScoringResultVOPage(questionPage, request));
    }


    /**
     * 编辑结果分数表（给用户使用）
     *
     * @param scoringResultEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editScoringResult(
            @RequestBody ScoringResultEditRequest scoringResultEditRequest, HttpServletRequest request) {
        if (scoringResultEditRequest == null || scoringResultEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultEditRequest, scoringResult);
        String resultProp = JSONUtil.toJsonStr(scoringResultEditRequest.getResultProp());
        scoringResult.setResultProp(resultProp);
        // 数据校验
        scoringResultService.validScoringResult(scoringResult, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = scoringResultEditRequest.getId();
        ScoringResult oldScoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldScoringResult.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = scoringResultService.updateById(scoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
