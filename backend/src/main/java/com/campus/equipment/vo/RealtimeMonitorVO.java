package com.campus.equipment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RealtimeMonitorVO {
    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
    private long faultDevices;
    private long unhandledAlerts;
    private BigDecimal onlineRate = BigDecimal.ZERO;
    private BigDecimal averageHealthScore = BigDecimal.ZERO;
    private LocalDateTime refreshTime;
    private List<DeviceRealtimeItem> devices = new ArrayList<>();
    private List<DeviceRealtimeItem> riskDevices = new ArrayList<>();
    private List<AlertItem> alerts = new ArrayList<>();
    private List<TrendPoint> trend = new ArrayList<>();

    @Data
    public static class DeviceRealtimeItem {
        private Long id;
        private String deviceCode;
        private String deviceName;
        private Integer status;
        private Integer onlineStatus;
        private BigDecimal cpuUsage;
        private BigDecimal memoryUsage;
        private BigDecimal diskUsage;
        private BigDecimal temperature;
        private Integer networkStatus;
        private LocalDateTime recordTime;
        private Integer healthScore;
        private String riskLevel;
        private String riskLabel;
        private String maintenanceAdvice;
    }

    @Data
    public static class AlertItem {
        private Long id;
        private Long deviceId;
        private String deviceCode;
        private String deviceName;
        private String alertType;
        private Integer alertLevel;
        private String alertMsg;
        private LocalDateTime createTime;
    }

    @Data
    public static class TrendPoint {
        private String label;
        private BigDecimal cpuUsage;
        private BigDecimal memoryUsage;
        private BigDecimal temperature;
    }
}
