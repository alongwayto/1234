package com.campus.equipment.service;

import com.campus.equipment.entity.AiChatMessage;
import com.campus.equipment.entity.AiChatSession;

import java.util.List;

/**
 * AI 智能聊天服务接口
 */
public interface AIChatService {

    /**
     * 创建新会话
     */
    AiChatSession createSession(Long userId, String sessionType, Long deviceId, Long faultId);

    /**
     * 获取用户会话列表
     */
    List<AiChatSession> getUserSessions(Long userId);

    /**
     * 获取会话消息历史
     */
    List<AiChatMessage> getSessionMessages(Long sessionId);

    /**
     * 发送消息并获取AI回复
     */
    AiChatMessage sendMessage(Long sessionId, String content);

    /**
     * 删除会话
     */
    void deleteSession(Long sessionId);

    /**
     * 更新会话活跃时间
     */
    void updateSessionActivity(Long sessionId);
}
