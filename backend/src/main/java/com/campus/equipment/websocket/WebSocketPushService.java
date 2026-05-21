package com.campus.equipment.websocket;

import com.campus.equipment.vo.RealtimeMonitorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息推送服务
 * 负责推送设备状态、预警、工单等实时消息到前端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketPushService {

    private final SimpMessagingTemplate messagingTemplate;

    // 在线用户会话缓存
    private final Map<String, String> onlineSessions = new ConcurrentHashMap<>();

    // ==================== 设备状态实时推送 ====================

    /**
     * 推送设备状态更新
     */
    public void pushDeviceStatusUpdate(RealtimeMonitorVO.DeviceStatus deviceStatus) {
        messagingTemplate.convertAndSend("/topic/device/status/" + deviceStatus.getDeviceId(), deviceStatus);
        messagingTemplate.convertAndSend("/topic/device/status/all", deviceStatus);
        log.debug("推送设备状态更新: deviceId={}", deviceStatus.getDeviceId());
    }

    /**
     * 推送所有设备状态概览
     */
    public void pushDeviceStatusOverview(RealtimeMonitorVO overview) {
        messagingTemplate.convertAndSend("/topic/device/overview", overview);
        log.debug("推送设备状态概览");
    }

    /**
     * 推送单个设备状态到所有订阅者
     */
    public void pushToAllDeviceStatus(String deviceId, Object data) {
        messagingTemplate.convertAndSend("/topic/device/status/" + deviceId, data);
    }

    // ==================== 预警消息推送 ====================

    /**
     * 推送新预警通知
     */
    public void pushNewAlert(Map<String, Object> alert) {
        messagingTemplate.convertAndSend("/topic/alert/new", alert);
        log.info("推送新预警: alertId={}, level={}", alert.get("id"), alert.get("alertLevel"));
    }

    /**
     * 推送预警状态更新
     */
    public void pushAlertStatusUpdate(Map<String, Object> update) {
        messagingTemplate.convertAndSend("/topic/alert/update", update);
    }

    /**
     * 推送未处理预警数量
     */
    public void pushUnhandledAlertCount(long count) {
        messagingTemplate.convertAndSend("/topic/alert/count", count);
    }

    // ==================== 工单消息推送 ====================

    /**
     * 推送新工单通知
     */
    public void pushNewWorkOrder(Map<String, Object> workOrder) {
        messagingTemplate.convertAndSend("/topic/workorder/new", workOrder);
        log.info("推送新工单: orderNo={}", workOrder.get("orderNo"));
    }

    /**
     * 推送工单状态更新
     */
    public void pushWorkOrderUpdate(Map<String, Object> update) {
        messagingTemplate.convertAndSend("/topic/workorder/update", update);
    }

    // ==================== 故障消息推送 ====================

    /**
     * 推送新故障上报
     */
    public void pushNewFaultReport(Map<String, Object> faultReport) {
        messagingTemplate.convertAndSend("/topic/fault/new", faultReport);
        log.info("推送新故障上报: faultNo={}", faultReport.get("faultNo"));
    }

    /**
     * 推送AI诊断结果
     */
    public void pushAIDiagnosisResult(String sessionId, Map<String, Object> diagnosis) {
        messagingTemplate.convertAndSend("/topic/ai/diagnosis/" + sessionId, diagnosis);
    }

    // ==================== 系统消息推送 ====================

    /**
     * 推送系统通知到指定用户
     */
    public void pushToUser(String userId, String message) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notification", message);
    }

    /**
     * 推送系统广播消息
     */
    public void pushBroadcast(String message) {
        messagingTemplate.convertAndSend("/topic/system/broadcast", message);
    }

    // ==================== 会话管理 ====================

    /**
     * 记录用户上线
     */
    public void onUserOnline(String sessionId, String userId) {
        onlineSessions.put(sessionId, userId);
        log.info("用户上线: userId={}, sessionId={}", userId, sessionId);
        // 广播在线用户数量
        messagingTemplate.convertAndSend("/topic/online/count", onlineSessions.size());
    }

    /**
     * 记录用户下线
     */
    public void onUserOffline(String sessionId) {
        String userId = onlineSessions.remove(sessionId);
        if (userId != null) {
            log.info("用户下线: userId={}", userId);
            messagingTemplate.convertAndSend("/topic/online/count", onlineSessions.size());
        }
    }

    /**
     * 获取当前在线人数
     */
    public int getOnlineCount() {
        return onlineSessions.size();
    }
}
