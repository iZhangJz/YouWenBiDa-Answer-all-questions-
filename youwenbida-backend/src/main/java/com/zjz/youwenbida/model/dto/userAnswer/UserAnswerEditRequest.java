package com.zjz.youwenbida.model.dto.userAnswer;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑用户答题表请求 - 用户
 */
@Data
public class UserAnswerEditRequest implements Serializable {

    /**
     * 用户答案表 id
     */
    private Long id;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;


    private static final long serialVersionUID = 1L;
}