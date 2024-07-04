package com.zjz.youwenbida.scoring.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zjz.youwenbida.annotation.ScoringStrategyCheck;
import com.zjz.youwenbida.constant.AppScoringStrategyConstant;
import com.zjz.youwenbida.constant.AppTypeConstant;
import com.zjz.youwenbida.manager.AIManager;
import com.zjz.youwenbida.model.dto.question.QuestionAnswerDTO;
import com.zjz.youwenbida.model.dto.question.QuestionContentDTO;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.Question;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.zjz.youwenbida.scoring.IScoringStrategy;
import com.zjz.youwenbida.service.QuestionService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.zjz.youwenbida.constant.AIPromptConstant.AI_TEST_SCORING_SYS_PROMPT;

/**
 * AI 测评类评分策略
 */
@ScoringStrategyCheck(appType = AppTypeConstant.TEST,scoringStrategy = AppScoringStrategyConstant.AI)
public class AIAssessmentScoring implements IScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AIManager aiManager;

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
        //3.封装用户 Prompt
        List<String> answerValue = getAnswerValue(choices, questionContentDTOS);
        String userMessage = getAiTestScoringUserMessage(app, questionContentDTOS, answerValue);
       // 4.调用 AI 接口，获取 AI 的回答
        String AIResult = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYS_PROMPT, userMessage);
        int start = AIResult.indexOf('{');
        int end = AIResult.lastIndexOf('}');
        AIResult = AIResult.substring(start, end + 1);

        UserAnswer userAnswer = JSONUtil.toBean(AIResult, UserAnswer.class);
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        return userAnswer;
    }


    private static List<String> getAnswerValue(List<String> choices,List<QuestionContentDTO> questionContentDTOS){
        List<String> resultValue = new ArrayList<>();
        for (int i = 0;i < choices.size();i++){
            String choice = choices.get(i);
            // 获取对应的题目
            QuestionContentDTO questionContentDTO = questionContentDTOS.get(i);
            // 遍历该题目的所有选项
            List<QuestionContentDTO.Option> options = questionContentDTO.getOptions();
            for (QuestionContentDTO.Option option : options) {
                if (option.getKey().equals(choice)) {
                    String value = option.getValue();
                    resultValue.add(value);
                }
            }
        }
        return resultValue;
    }

    private String getAiTestScoringUserMessage(
            App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            // 获取用户答案对应的选项内容

            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }
}
