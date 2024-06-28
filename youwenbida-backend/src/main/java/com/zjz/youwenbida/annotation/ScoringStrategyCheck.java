package com.zjz.youwenbida.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @Description: 策略校验
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScoringStrategyCheck {

    /**
     * 应用类型
     */
    int appType();

    /**
     * 评分策略类型
     */
    int scoringStrategy();
}
