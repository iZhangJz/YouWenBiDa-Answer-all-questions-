package com.zjz.youwenbida.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhipu.oapi.service.v4.model.ModelData;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.constant.AIPromptConstant;
import com.zjz.youwenbida.constant.CommonConstant;
import com.zjz.youwenbida.exception.ThrowUtils;
import com.zjz.youwenbida.manager.AIManager;
import com.zjz.youwenbida.mapper.QuestionMapper;
import com.zjz.youwenbida.model.dto.question.QuestionQueryRequest;
import com.zjz.youwenbida.model.entity.Question;
import com.zjz.youwenbida.model.entity.User;
import com.zjz.youwenbida.model.vo.QuestionVO;
import com.zjz.youwenbida.model.vo.UserVO;
import com.zjz.youwenbida.service.QuestionService;
import com.zjz.youwenbida.service.UserService;
import com.zjz.youwenbida.utils.SqlUtils;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * 问题表服务实现
 *
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private AIManager aiManager;
    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        String questionContent = question.getQuestionContent();
        Long appId = question.getAppId();
        Long userId = question.getUserId();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(ObjectUtils.isEmpty(questionContent),
                    ErrorCode.PARAMS_ERROR,"问题内容不能为空");
            ThrowUtils.throwIf(ObjectUtils.isEmpty(appId), ErrorCode.PARAMS_ERROR,"应用ID不能为空");
            ThrowUtils.throwIf(ObjectUtils.isEmpty(userId), ErrorCode.PARAMS_ERROR,"用户ID不能为空");
        }
        // 修改数据时，有参数则校验
        if(ObjectUtils.isNotEmpty(appId)){
            ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR,"应用ID不存在");
        }
        if(ObjectUtils.isNotEmpty(userId)) {
            ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR,"用户ID不存在");
        }

    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        Long appId = questionQueryRequest.getAppId();
        Long userId = questionQueryRequest.getUserId();
        Date createTime = questionQueryRequest.getCreateTime();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 排序
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
            queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                    sortField);
        }

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("questionContent", searchText));
        }
        // 模糊查询
        queryWrapper.like(ObjectUtils.isNotEmpty(createTime), "createTime", createTime);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        return queryWrapper;
    }

    /**
     * 获取问题表封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        return questionVO;
    }

    /**
     * 分页获取问题表封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());

        // 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
        });
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    @Override
    public SseEmitter generateAIQuestionSSE(String userPrompt) {
        // 3.建立 SSE 连接对象，0 表示永久不超时
        SseEmitter emitter = new SseEmitter(0L);
        // 4.调用 AI 生成问题,获取流式数据
        Flowable<ModelData> modelDataFlowable = aiManager.doStreamStableRequest(
                AIPromptConstant.AI_GENERATE_QUESTION_SYS_PROMPT, userPrompt);
        // 5.解析 AI 返回的 json 数据
        // 定义一个StringBuilder类型的变量contentBuilder，用于存储消息内容
        StringBuilder contentBuilder = new StringBuilder();
        // 定义一个AtomicInteger类型的变量flag，用于记录消息中"{}"的数量
        AtomicInteger flag = new AtomicInteger(0);
        // 使用modelDataFlowable将消息分片，并获取每个分片的第一个choice的delta内容
        modelDataFlowable
                .observeOn(Schedulers.io())
                .map(chunk -> chunk.getChoices().get(0).getDelta().getContent())
                // 将消息中的空格替换为空字符
                .map(msg -> msg.replaceAll("\\s", ""))
                // 过滤掉空字符
                .filter(StrUtil::isNotBlank)
                // 将消息转换为字符列表
                .flatMap(msg -> {
                    List<Character> characterList = new ArrayList<>();
                    for (Character ch : msg.toCharArray()) {
                        characterList.add(ch);
                    }
                    return Flowable.fromIterable(characterList);
                })
                // 当遇到"{"时，将flag加1，当遇到"}"时，将flag减1，当flag为0时，将contentBuilder中的内容发送给emitter
                .doOnNext(ch -> {
                    if (ch == '{') {
                        flag.incrementAndGet();
                    } else if (ch == '}') {
                        flag.decrementAndGet();
                        if (flag.get() == 0) {
                            contentBuilder.append(ch);
                            emitter.send(JSONUtil.toJsonStr(contentBuilder.toString()));
                            contentBuilder.setLength(0);
                        }
                    }
                    if (flag.get() > 0) {
                        contentBuilder.append(ch);
                    }
                })
                // 当所有消息发送完毕时，调用emitter的complete方法
                .doOnComplete(emitter::complete)
                // 订阅消息
                .subscribe();
        return emitter;
    }

}
