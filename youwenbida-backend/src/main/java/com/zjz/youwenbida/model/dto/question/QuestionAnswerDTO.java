package com.zjz.youwenbida.model.dto.question;

import lombok.Data;

/**
 * 用户回答应用得到的答案
 */
@Data
public class QuestionAnswerDTO {
    /**
     * 题目内容
     */
    private String title;

    /**
     * 用户给出的答案
     */
    private String userAnswer;
}
