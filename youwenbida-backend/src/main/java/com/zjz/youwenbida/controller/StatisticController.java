package com.zjz.youwenbida.controller;

import com.zjz.youwenbida.common.BaseResponse;
import com.zjz.youwenbida.common.ResultUtils;
import com.zjz.youwenbida.mapper.UserAnswerMapper;
import com.zjz.youwenbida.model.dto.statistic.AppAnswerCountDTO;
import com.zjz.youwenbida.model.dto.statistic.AppAnswerResultCountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/app/statistic")
public class StatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @GetMapping("/top/12")
    public BaseResponse<List<AppAnswerCountDTO>> getAppTop12(){
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    @GetMapping
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppAnswerResultCount(Long appId){
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }
}
