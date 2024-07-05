package com.zjz.youwenbida.manager;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import com.zjz.youwenbida.common.ErrorCode;
import com.zjz.youwenbida.exception.BusinessException;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AIManager {

    @Resource
    private ClientV4 clientV4;

    /**
     * 稳定的温度，用于生成更稳定、更一致的回复。
     */
    private static final float STABLE_TEMPERATURE = 0.05f;
    /**
     * 创造性的温度，用于生成更多有趣、多样化的回复。
     */
    private static final float CREATIVE_TEMPERATURE = 0.95f;

    /**
     * AI 通用请求方法
     * @param messages
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream,Float temperature){
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        String res;
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            res = invokeModelApiResp.getData().getChoices().get(0).getMessage().toString();
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 请求失败");
        }
        return res;
    }

    /**
     * 传递了用户消息和系统消息的请求方法
     * @param sysMessage
     * @param userMessage
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(String sysMessage, String userMessage, Boolean stream,Float temperature) {
        List<ChatMessage> chatMsgList = createChatMsgList(sysMessage, userMessage);
        return doRequest(chatMsgList, stream, temperature);
    }

    /**
     * 同步请求
     * @param sysMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public String doSyncRequest(String sysMessage, String userMessage,Float temperature){
        return doRequest(sysMessage,userMessage,Boolean.FALSE,temperature);
    }


    /**
     * 稳定消息同步请求
     * @param sysMessage
     * @param userMessage
     * @return
     */
    public String doSyncStableRequest(String sysMessage, String userMessage){
        return doSyncRequest(sysMessage,userMessage,STABLE_TEMPERATURE);
    }

    /**
     * 创造性的消息同步请求
     * @param sysMessage
     * @param userMessage
     * @return
     */
    public String doSyncCreativeRequest(String sysMessage, String userMessage){
        return doSyncRequest(sysMessage,userMessage,CREATIVE_TEMPERATURE);
    }

    /**
     * 通用流式请求
     * @param messages
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages,Float temperature){
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(true)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getFlowable();
    }

    /**
     * 通用流式请求,简化消息传递
     * @param sysMessage
     * @param userMessage
     * @return
     */
    public Flowable<ModelData> doStreamStableRequest(String sysMessage, String userMessage){
        List<ChatMessage> messageList = createChatMsgList(sysMessage, userMessage);
        return doStreamRequest(messageList,STABLE_TEMPERATURE);
    }

    /**
     * 构造AI消息
     * @param sysMessage
     * @param userMessage
     * @return
     */
    private List<ChatMessage> createChatMsgList(String sysMessage, String userMessage) {
        // string 非空判断;若String 为空，则抛出异常
        if (sysMessage == null || sysMessage.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "系统消息不能为空");
        }
        if (userMessage == null || userMessage.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        }
        List<ChatMessage> messageList = new ArrayList<>();
        messageList.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), sysMessage));
        messageList.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));
        return messageList;
    }
}
