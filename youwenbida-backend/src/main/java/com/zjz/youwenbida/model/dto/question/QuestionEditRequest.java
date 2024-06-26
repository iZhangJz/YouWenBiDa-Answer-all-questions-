package com.zjz.youwenbida.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑问题表请求
 */
@Data
public class QuestionEditRequest implements Serializable {

    /**
     * 问题id
     */
    private Long id;

    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContent;


    private static final long serialVersionUID = 1L;
}