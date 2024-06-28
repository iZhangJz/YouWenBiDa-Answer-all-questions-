package com.zjz.youwenbida.scoring;

import com.zjz.youwenbida.annotation.ScoringStrategyCheck;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.exception.BusinessException;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScoringStrategyExecutor {

    /**
     * 策略列表
     */
    @Resource
    private List<IScoringStrategy> scoringStrategies;


    public UserAnswer doDispatch(List<String> choices, App app){
        // 1.获取App应用类型 和 评分策略类型
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        if (appType == null || scoringStrategy == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用配置错误");
        }
        // 2.根据策略类型 获取对应的策略
        for (IScoringStrategy strategy : scoringStrategies) {
            // 先确认该对象是否有注解 -> 通过反射机制获取注解
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyCheck.class)){
                ScoringStrategyCheck check = strategy.getClass().getAnnotation(ScoringStrategyCheck.class);
                // 判断注解的类型是否和 应用的类型一致
                if (check.appType() == appType && check.scoringStrategy() == scoringStrategy){
                    // 执行策略
                    return strategy.doScoring(choices,app);
                }
            }
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用配置错误");
    }
}
