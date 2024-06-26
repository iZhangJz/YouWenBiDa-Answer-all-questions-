package com.zjz.youwenbida.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求
 */
@Data
public class ReviewRequest implements Serializable {
    /**
     * 业务类型 id
     */
    private Long id;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核意见
     */
    private String reviewMessage;


    private static final long serialVersionUID = 1L;
}
