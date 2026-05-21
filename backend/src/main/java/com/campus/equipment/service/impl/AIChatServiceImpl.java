package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.equipment.entity.AiChatMessage;
import com.campus.equipment.entity.AiChatSession;
import com.campus.equipment.mapper.AiChatMessageMapper;
import com.campus.equipment.mapper.AiChatSessionMapper;
import com.campus.equipment.service.AIChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 智能聊天服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final com.campus.equipment.ai.AIDiagnosisService aiDiagnosisService;

    @Override
    @Transactional
    public AiChatSession createSession(Long userId, String sessionType, Long deviceId, Long faultId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setSessionType(sessionType != null ? sessionType : "general");
        session.setDeviceId(deviceId);
        session.setFaultId(faultId);
        session.setTitle("新会话");
        session.setMessageCount(0);
        session.setLastActiveTime(LocalDateTime.now());
        
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public List<AiChatSession> getUserSessions(Long userId) {
        QueryWrapper<AiChatSession> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("deleted", 0);
        query.orderByDesc("last_active_time");
        return sessionMapper.selectList(query);
    }

    @Override
    public List<AiChatMessage> getSessionMessages(Long sessionId) {
        QueryWrapper<AiChatMessage> query = new QueryWrapper<>();
        query.eq("session_id", sessionId);
        query.eq("deleted", 0);
        query.orderBy("create_time");
        return messageMapper.selectList(query);
    }

    @Override
    @Transactional
    public AiChatMessage sendMessage(Long sessionId, String content) {
        long startTime = System.currentTimeMillis();
        
        // 1. 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setMessageType("text");
        userMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMessage);
        
        // 2. 获取会话上下文
        AiChatSession session = sessionMapper.selectById(sessionId);
        String context = buildContext(session);
        
        // 3. 调用 AI 服务获取回复
        String aiResponse = aiDiagnosisService.answerQuestion(content, context);
        
        // 4. 保存 AI 回复
        AiChatMessage aiMessage = new AiChatMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        aiMessage.setMessageType("text");
        aiMessage.setCostTime(System.currentTimeMillis() - startTime);
        aiMessage.setCreateTime(LocalDateTime.now());
        messageMapper.insert(aiMessage);
        
        // 5. 更新会话信息
        updateSessionActivity(sessionId);
        
        // 如果是首条消息，更新会话标题
        if (session.getMessageCount() == 0 && content.length() > 20) {
            session.setTitle(content.substring(0, 20) + "...");
            sessionMapper.updateById(session);
        }
        
        return aiMessage;
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        // 删除会话
        sessionMapper.deleteById(sessionId);
        
        // 删除关联消息
        QueryWrapper<AiChatMessage> query = new QueryWrapper<>();
        query.eq("session_id", sessionId);
        messageMapper.delete(query);
    }

    @Override
    @Transactional
    public void updateSessionActivity(Long sessionId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setMessageCount(session.getMessageCount() + 1);
            session.setLastActiveTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
    }

    /**
     * 构建上下文信息
     */
    private String buildContext(AiChatSession session) {
        StringBuilder context = new StringBuilder();
        
        if (session.getDeviceId() != null) {
            context.append("【关联设备】ID: ").append(session.getDeviceId()).append("\n");
        }
        if (session.getFaultId() != null) {
            context.append("【关联故障】ID: ").append(session.getFaultId()).append("\n");
        }
        
        // 添加最近的消息历史
        List<AiChatMessage> history = getSessionMessages(session.getId());
        if (!history.isEmpty()) {
            context.append("【对话历史】\n");
            int count = 0;
            for (AiChatMessage msg : history) {
                if (count++ >= 10) break; // 只取最近10条
                String role = "user".equals(msg.getRole()) ? "用户" : "助手";
                context.append(role).append(": ").append(msg.getContent()).append("\n");
            }
        }
        
        return context.toString();
    }
}
