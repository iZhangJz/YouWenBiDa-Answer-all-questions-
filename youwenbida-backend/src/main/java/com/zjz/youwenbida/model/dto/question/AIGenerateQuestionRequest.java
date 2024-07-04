package com.zjz.youwenbida.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 生成题目请求
 */
@Data
public class AIGenerateQuestionRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 题目数量
     */
    private int questionNumber;

    /**
     * 每一道题目的选项数量
     */
    private int optionNumber;

    private static final long serialVersionUID = 1L;
}
