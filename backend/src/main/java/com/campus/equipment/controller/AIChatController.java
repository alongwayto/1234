package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.entity.AiChatMessage;
import com.campus.equipment.entity.AiChatSession;
import com.campus.equipment.service.AIChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 智能聊天控制器
 */
@Tag(name = "AI智能聊天")
@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @Operation(summary = "创建新会话")
    @PostMapping("/session")
    public Result<AiChatSession> createSession(@RequestBody CreateSessionRequest request) {
        AiChatSession session = aiChatService.createSession(
            request.getUserId(),
            request.getSessionType(),
            request.getDeviceId(),
            request.getFaultId()
        );
        return Result.success(session);
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public Result<List<AiChatSession>> getSessions(Long userId) {
        List<AiChatSession> sessions = aiChatService.getUserSessions(userId);
        return Result.success(sessions);
    }

    @Operation(summary = "获取会话消息")
    @GetMapping("/messages/{sessionId}")
    public Result<List<AiChatMessage>> getMessages(@PathVariable Long sessionId) {
        List<AiChatMessage> messages = aiChatService.getSessionMessages(sessionId);
        return Result.success(messages);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send/{sessionId}")
    public Result<AiChatMessage> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody SendMessageRequest request) {
        AiChatMessage response = aiChatService.sendMessage(sessionId, request.getContent());
        return Result.success(response);
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/session/{sessionId}")
    public Result<String> deleteSession(@PathVariable Long sessionId) {
        aiChatService.deleteSession(sessionId);
        return Result.success("删除成功");
    }

    @Data
    public static class CreateSessionRequest {
        private Long userId;
        private String sessionType;
        private Long deviceId;
        private Long faultId;
    }

    @Data
    public static class SendMessageRequest {
        private String content;
    }
}
