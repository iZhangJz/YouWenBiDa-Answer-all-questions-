package com.zjz.youwenbida.scoring.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjz.youwenbida.annotation.ScoringStrategyCheck;
import com.zjz.youwenbida.constant.AppScoringStrategyConstant;
import com.zjz.youwenbida.constant.AppTypeConstant;
import com.zjz.youwenbida.model.dto.question.QuestionContentDTO;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.Question;
import com.zjz.youwenbida.model.entity.ScoringResult;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.zjz.youwenbida.scoring.IScoringStrategy;
import com.zjz.youwenbida.service.QuestionService;
import com.zjz.youwenbida.service.ScoringResultService;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 用户自定义测评类评分策略
 */
@ScoringStrategyCheck(appType = AppTypeConstant.TEST,scoringStrategy = AppScoringStrategyConstant.CUSTOMIZE)
public class CustomAssessmentScoring implements IScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScoring(List<String> choices, App app) {
        // 1.获取应用对应的题目
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class)
                        .eq(Question::getAppId, appId)
        );
        // 2.获取题目中的 questionContent 即具体的题目内容
        // 2.1 将 questionContent 转换为 List<QuestionContentDTO> 数组
        List<QuestionContentDTO> questionContentDTOS;
        if (question.getQuestionContent() != null){
            questionContentDTOS = JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class);
        }else {
            throw new RuntimeException("题目内容为空");
        }

        HashMap<String, Integer> propResult = getPropResult(choices, questionContentDTOS);
        // 6.获取应用对应的评分结果 List<ScoringResult>
        List<ScoringResult> scoringResults = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
        );

        int maxScore = Integer.MIN_VALUE;
        ScoringResult bestResult = new ScoringResult();
        // 7.遍历 List<ScoringResult>
        // 7.1 对于每一个 ScoringResult 对象，获取 resultProp 对象
        // 7.2 将 resultProp 转换为 List<String>
        for (ScoringResult scoringResult : scoringResults) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            int score = resultProp.stream().mapToInt(prop -> propResult.getOrDefault(prop, 0)).sum();
            if (score > maxScore){
                maxScore = score;
                bestResult = scoringResult;
            }
        }

        // 9.获取最大值对应的 resultProp 对象
        // 9.1 构造 UserAnswer 并返回
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(bestResult.getId());
        userAnswer.setResultName(bestResult.getResultName());
        userAnswer.setResultDesc(bestResult.getResultDesc());
        userAnswer.setResultPicture(bestResult.getResultPicture());

        return userAnswer;
    }

    @NotNull
    private static HashMap<String, Integer> getPropResult(
            List<String> choices, List<QuestionContentDTO> questionContentDTOS) {
        HashMap<String, Integer> propResult = new HashMap<>();

        // 3.遍历用户提交的答案 choices 答案是顺序的 一个 choice 对应一个题目
        for (int i = 0;i < choices.size();i++){
            String choice = choices.get(i);
            // 获取对应的题目
            QuestionContentDTO questionContentDTO = questionContentDTOS.get(i);
            // 遍历该题目的所有选项
            List<QuestionContentDTO.Option> options = questionContentDTO.getOptions();
            for (QuestionContentDTO.Option option : options) {
                if (option.getKey().equals(choice)) {
                    String result = option.getResult();
                    if (propResult.containsKey(result)) {
                        propResult.put(result, propResult.get(result) + 1);
                    } else {
                        propResult.put(result, 1);
                    }
                }
            }
        }
        return propResult;
    }
}
