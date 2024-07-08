package com.zjz.youwenbida.mapper;

import com.zjz.youwenbida.model.dto.statistic.AppAnswerCountDTO;
import com.zjz.youwenbida.model.dto.statistic.AppAnswerResultCountDTO;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Zhang
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2024-06-25 15:38:05
* @Entity com.zjz.youwenbida.model.entity.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    /**
     * 查询答题数量最多的前12个应用
     * @return List<AppAnswerCountDTO>
     */
    List<AppAnswerCountDTO> doAppAnswerCount();

    /**
     * 查询单个应用的答题情况分布
     * @param appId 应用Id
     * @return List<AppAnswerResultCountDTO>
     */
    List<AppAnswerResultCountDTO> doAppAnswerResultCount(Long appId);

}




