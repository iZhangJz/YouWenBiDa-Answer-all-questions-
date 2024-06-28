package com.zjz.youwenbida.scoring.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjz.youwenbida.annotation.ScoringStrategyCheck;
import com.zjz.youwenbida.model.dto.question.QuestionContentDTO;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.Question;
import com.zjz.youwenbida.model.entity.ScoringResult;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.zjz.youwenbida.scoring.IScoringStrategy;
import com.zjz.youwenbida.service.QuestionService;
import com.zjz.youwenbida.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户自定义得分类评分策略
 */
@ScoringStrategyCheck(appType = 0, scoringStrategy = 0)
public class CustomMarkScoring implements IScoringStrategy {
    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScoring(List<String> choices, App app) {
        // 1.获取App对应的问题 question
        // 1.1 获取 question 对应的 questionContent
        // 1.2 将 questionContent 转换为 List<QuestionContentDTO>
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class)
                        .eq(Question::getAppId, appId)
        );


        List<QuestionContentDTO> questionContentDTOS
                = JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class);

        // 2.遍历 List<QuestionContentDTO>
        int score = getScore(choices, questionContentDTOS);
        // 3.根据 questionContent 中的 scoringRange 进行评分

        List<ScoringResult> scoringResults = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );
        ScoringResult bestResult = new ScoringResult();
        for (ScoringResult scoringResult : scoringResults) {
            if (score >= scoringResult.getResultScoreRange()){
                bestResult = scoringResult;
                break;
            }
        }

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(bestResult.getId());
        userAnswer.setResultName(bestResult.getResultName());
        userAnswer.setResultDesc(bestResult.getResultDesc());
        userAnswer.setResultPicture(bestResult.getResultPicture());
        userAnswer.setResultScore(score);
        return userAnswer;
    }

    private static int getScore(List<String> choices, List<QuestionContentDTO> questionContentDTOS) {
        int score = 0;
        for (int i = 0; i < questionContentDTOS.size(); i++){
            QuestionContentDTO questionContentDTO = questionContentDTOS.get(i);
            List<QuestionContentDTO.Option> options = questionContentDTO.getOptions();
            // 获取用户的 choice
            String choice = choices.get(i);
            for (QuestionContentDTO.Option option : options) {
                // 判断用户选择了哪个选项
                if (option.getKey().equals(choice)){
                    score += option.getScore();
                }
            }
        }
        return score;
    }
}
