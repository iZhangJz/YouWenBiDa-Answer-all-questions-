package com.zjz.youwenbida.service;

import com.zjz.youwenbida.model.entity.UserAnswer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 用户答案表分表测试
 */
@SpringBootTest
public class UserAnswerShardingTest {
    @Resource
    private UserAnswerService userAnswerService;

    @Test
    public void test(){
        UserAnswer userAnswer1 = new UserAnswer();
        userAnswer1.setAppId(10L);
        userAnswer1.setUserId(1L);
        userAnswer1.setChoices("1,1,1,1");

        userAnswerService.save(userAnswer1);

        UserAnswer userAnswer2 = new UserAnswer();
        userAnswer2.setAppId(11L);
        userAnswer2.setUserId(1L);
        userAnswer2.setChoices("2,2,2,2");

        userAnswerService.save(userAnswer2);
    }
}
