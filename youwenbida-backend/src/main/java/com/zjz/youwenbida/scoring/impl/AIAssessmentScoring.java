package com.zjz.youwenbida.scoring.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zjz.youwenbida.annotation.ScoringStrategyCheck;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.constant.AppScoringStrategyConstant;
import com.zjz.youwenbida.constant.AppTypeConstant;
import com.zjz.youwenbida.constant.RedisConstant;
import com.zjz.youwenbida.exception.BusinessException;
import com.zjz.youwenbida.manager.AIManager;
import com.zjz.youwenbida.model.dto.question.QuestionAnswerDTO;
import com.zjz.youwenbida.model.dto.question.QuestionContentDTO;
import com.zjz.youwenbida.model.entity.App;
import com.zjz.youwenbida.model.entity.Question;
import com.zjz.youwenbida.model.entity.UserAnswer;
import com.zjz.youwenbida.scoring.IScoringStrategy;
import com.zjz.youwenbida.service.QuestionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedissonClient redissonClient;

    /**
     * Caffeine 本地缓存AI评分结果
     * 缓存同应用同答案评分结果
     */
    private final Cache<String,String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024) // 1024 个键值对数量
                    .expireAfterAccess(5L, TimeUnit.MINUTES)    // 5 分钟后清除缓存
                    .build();

    /**
     *  构建缓存key
     * @param appId
     * @param choices
     * @return
     */
    private String buildCacheKey(Long appId,List<String> choices){
        String jsonStr = JSONUtil.toJsonStr(choices);
        String choiceMd5 = Arrays.toString(DigestUtil.md5(jsonStr));
        return appId + ":" + choiceMd5;
    }


    @Override
    public UserAnswer doScoring(List<String> choices, App app) {
        // 1.获取应用对应的题目
        Long appId = app.getId();
        // 从缓存中查找数据
        String cacheKey = buildCacheKey(appId, choices);
        String cacheValue = answerCacheMap.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cacheValue)){
            // 缓存不为空 提前返回数据
            UserAnswer userAnswer = JSONUtil.toBean(cacheValue, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(JSONUtil.toJsonStr(choices));
            return userAnswer;
        }
        // 定义锁
        RLock lock = redissonClient.getLock(RedisConstant.AI_ANSWER_LOCK + cacheKey);
        try{
            // 尝试获取锁 获取锁成功开启 watchDog 机制
            boolean res = lock.tryLock(5L, TimeUnit.SECONDS);
            if (!res){
                // 获取锁失败
                return null;
            }
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

            // 查询缓存未命中 设置缓存
            answerCacheMap.put(cacheKey,AIResult);

            UserAnswer userAnswer = JSONUtil.toBean(AIResult, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(JSONUtil.toJsonStr(choices));
            return userAnswer;

        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取锁失败");
        } finally {
            // 释放锁
            if (lock != null && lock.isLocked()){
                // 判断锁是否是当前线程
                if (lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }
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
