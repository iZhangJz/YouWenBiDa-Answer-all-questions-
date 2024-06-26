package com.zjz.youwenbida.utils;

import com.zjz.youwenbida.model.enums.AppTypeEnum;
import com.zjz.youwenbida.model.enums.ReviewStatusEnum;
import com.zjz.youwenbida.model.enums.ScoringStrategyEnum;

public class ValidEnumUtils {

    /**
     * 校验appType是否为有效枚举
     * @param appType
     * @return
     */
    public static boolean validAppTypeEnum(Integer appType){
        if(appType == null){
            return true;
        }
        AppTypeEnum enumByValue = AppTypeEnum.getEnumByValue(appType);
        return enumByValue == null;
    }

    /**
     * 校验scoringStrategy是否为有效枚举
     * @param scoringStrategy
     * @return
     */
    public static boolean validScoringStrategyEnum(Integer scoringStrategy){
        if(scoringStrategy == null){
            return true;
        }
        ScoringStrategyEnum enumByValue = ScoringStrategyEnum.getEnumByValue(scoringStrategy);
        return enumByValue == null;
    }

    /**
     * 校验reviewStatus是否为有效枚举
     * @param reviewStatus
     * @return
     */
    public static boolean validReviewStatusEnum(Integer reviewStatus){
        if (reviewStatus == null) {
            return true;
        }
        ReviewStatusEnum enumByValue = ReviewStatusEnum.getEnumByValue(reviewStatus);
        return enumByValue == null;
    }

}
