package com.campus.equipment.websocket;

import com.campus.equipment.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket 消息控制器
 * 处理客户端发送的消息
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketPushService pushService;

    /**
     * 处理设备状态上报（模拟物联网设备）
     */
    @MessageMapping("/device/report")
    public void handleDeviceReport(@Payload Map<String, Object> report, 
                                   SimpMessageHeaderAccessor headerAccessor) {
        log.debug("收到设备状态上报: {}", report);
        // 转发到监控服务处理
        pushService.pushToAllDeviceStatus(
            String.valueOf(report.get("deviceId")), 
            report
        );
    }

    /**
     * 处理心跳消息
     */
    @MessageMapping("/heartbeat")
    @SendToUser("/queue/heartbeat")
    public Result<String> handleHeartbeat(@Payload Map<String, Object> heartbeat,
                                          SimpMessageHeaderAccessor headerAccessor) {
        return Result.success("pong");
    }

    /**
     * 订阅设备状态
     */
    @MessageMapping("/subscribe/device")
    public void handleSubscribeDevice(@Payload Map<String, Object> request,
                                      SimpMessageHeaderAccessor headerAccessor) {
        log.debug("客户端订阅设备: {}", request);
    }

    /**
     * 取消订阅设备
     */
    @MessageMapping("/unsubscribe/device")
    public void handleUnsubscribeDevice(@Payload Map<String, Object> request,
                                        SimpMessageHeaderAccessor headerAccessor) {
        log.debug("客户端取消订阅设备: {}", request);
    }
}
