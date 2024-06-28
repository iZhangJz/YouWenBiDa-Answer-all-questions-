package com.zjz.youwenbida.scoring;

import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.UserAnswer;

import java.util.List;


/**
 * @Description:  评分策略接口
 */
public interface IScoringStrategy {

    /**
     * 执行对应的评分策略
     * @param choices   用户选择的答案
     * @param app       对应的应用
     * @return          用户答题记录
     */
    UserAnswer doScoring(List<String> choices, App app);

}
