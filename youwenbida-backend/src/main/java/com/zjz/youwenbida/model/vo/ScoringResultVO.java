package com.zjz.youwenbida.model.vo;

import cn.hutool.json.JSONUtil;
import com.zjz.youwenbida.model.entity.ScoringResult;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 结果分数表视图
 *
 */
@Data
public class ScoringResultVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 结果名称，如物流师
     */
    private String resultName;

    /**
     * 结果描述
     */
    private String resultDesc;

    /**
     * 结果图片
     */
    private String resultPicture;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param scoringResultVO
     * @return
     */
    public static ScoringResult voToObj(ScoringResultVO scoringResultVO) {
        if (scoringResultVO == null) {
            return null;
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultVO, scoringResult);

        return scoringResult;
    }

    /**
     * 对象转封装类
     *
     * @param scoringResult
     * @return
     */
    public static ScoringResultVO objToVo(ScoringResult scoringResult) {
        if (scoringResult == null) {
            return null;
        }
        ScoringResultVO scoringResultVO = new ScoringResultVO();
        BeanUtils.copyProperties(scoringResult, scoringResultVO);
        return scoringResultVO;
    }
}
