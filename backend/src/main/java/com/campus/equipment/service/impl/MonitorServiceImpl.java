package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.entity.DeviceAlert;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.DeviceStatusRecord;
import com.campus.equipment.mapper.DeviceAlertMapper;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.DeviceStatusRecordMapper;
import com.campus.equipment.service.MonitorService;
import com.campus.equipment.vo.RealtimeMonitorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private static final BigDecimal CPU_THRESHOLD = new BigDecimal("90");
    private static final BigDecimal MEMORY_THRESHOLD = new BigDecimal("85");
    private static final BigDecimal DISK_THRESHOLD = new BigDecimal("90");
    private static final BigDecimal TEMP_THRESHOLD = new BigDecimal("70");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DeviceStatusRecordMapper statusMapper;
    private final DeviceAlertMapper alertMapper;
    private final DeviceInfoMapper deviceMapper;

    @Override
    public void reportStatus(DeviceStatusRecord record) {
        DeviceInfo device = deviceMapper.selectById(record.getDeviceId());
        if (device != null) {
            record.setDeviceCode(device.getDeviceCode());
            boolean online = record.getNetworkStatus() == null || record.getNetworkStatus() == 1;
            device.setOnlineStatus(online ? 1 : 0);
            if (!Integer.valueOf(3).equals(device.getStatus())) {
                device.setStatus(online ? 1 : 2);
            }
            deviceMapper.updateById(device);
        }
        statusMapper.insert(record);
        checkAndGenerateAlerts(record);
    }

    @Override
    public List<DeviceStatusRecord> getRecentStatus(Long deviceId, int limit) {
        return statusMapper.selectRecentByDeviceId(deviceId, limit);
    }

    @Override
    public RealtimeMonitorVO getRealtimeMonitor() {
        RealtimeMonitorVO vo = new RealtimeMonitorVO();
        vo.setRefreshTime(LocalDateTime.now());

        List<DeviceInfo> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<DeviceInfo>().orderByAsc(DeviceInfo::getId));
        vo.setTotalDevices(devices.size());

        long online = 0;
        long fault = 0;
        int healthTotal = 0;
        List<RealtimeMonitorVO.DeviceRealtimeItem> realtimeDevices = new ArrayList<>();

        for (DeviceInfo device : devices) {
            if (Integer.valueOf(1).equals(device.getOnlineStatus())) {
                online++;
            }
            if (device.getStatus() != null && device.getStatus() != 1) {
                fault++;
            }

            RealtimeMonitorVO.DeviceRealtimeItem item = new RealtimeMonitorVO.DeviceRealtimeItem();
            item.setId(device.getId());
            item.setDeviceCode(device.getDeviceCode());
            item.setDeviceName(device.getDeviceName());
            item.setStatus(device.getStatus());
            item.setOnlineStatus(device.getOnlineStatus());

            List<DeviceStatusRecord> latest = getRecentStatus(device.getId(), 1);
            if (!latest.isEmpty()) {
                DeviceStatusRecord status = latest.get(0);
                item.setCpuUsage(status.getCpuUsage());
                item.setMemoryUsage(status.getMemoryUsage());
                item.setDiskUsage(status.getDiskUsage());
                item.setTemperature(status.getTemperature());
                item.setNetworkStatus(status.getNetworkStatus());
                item.setRecordTime(status.getRecordTime());
            }

            int alertCount = countUnhandledAlerts(device.getId());
            int healthScore = calculateHealthScore(item, alertCount);
            item.setHealthScore(healthScore);
            item.setRiskLevel(riskLevel(healthScore));
            item.setRiskLabel(riskLabel(healthScore));
            item.setMaintenanceAdvice(maintenanceAdvice(item, alertCount));
            healthTotal += healthScore;
            realtimeDevices.add(item);
        }

        vo.setOnlineDevices(online);
        vo.setOfflineDevices(Math.max(0, devices.size() - online));
        vo.setFaultDevices(fault);
        vo.setUnhandledAlerts(countUnhandledAlerts());
        if (!devices.isEmpty()) {
            vo.setOnlineRate(BigDecimal.valueOf(online * 100.0 / devices.size()).setScale(1, RoundingMode.HALF_UP));
            vo.setAverageHealthScore(BigDecimal.valueOf(healthTotal * 1.0 / devices.size()).setScale(1, RoundingMode.HALF_UP));
        }
        vo.setDevices(realtimeDevices);
        vo.setRiskDevices(realtimeDevices.stream()
                .filter(item -> item.getHealthScore() != null && item.getHealthScore() < 85)
                .sorted(Comparator.comparing(RealtimeMonitorVO.DeviceRealtimeItem::getHealthScore))
                .limit(5)
                .collect(Collectors.toList()));
        vo.setAlerts(getLatestUnhandledAlerts());
        vo.setTrend(getRecentTrend());
        return vo;
    }

    @Override
    public Page<DeviceAlert> pageAlerts(int pageNum, int pageSize, Long deviceId, Integer status, Integer alertLevel) {
        Page<DeviceAlert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DeviceAlert> wrapper = new LambdaQueryWrapper<DeviceAlert>()
                .eq(deviceId != null, DeviceAlert::getDeviceId, deviceId)
                .eq(status != null, DeviceAlert::getStatus, status)
                .eq(alertLevel != null, DeviceAlert::getAlertLevel, alertLevel)
                .orderByDesc(DeviceAlert::getCreateTime);
        return alertMapper.selectPage(page, wrapper);
    }

    @Override
    public void handleAlert(Long alertId, String handler, String remark, Integer status) {
        DeviceAlert alert = alertMapper.selectById(alertId);
        if (alert != null) {
            alert.setStatus(status);
            alert.setHandler(handler);
            alert.setHandleRemark(remark);
            alert.setHandleTime(LocalDateTime.now());
            alertMapper.updateById(alert);
        }
    }

    @Override
    public void checkAndGenerateAlerts(DeviceStatusRecord record) {
        DeviceInfo device = deviceMapper.selectById(record.getDeviceId());
        String deviceName = device != null ? device.getDeviceName() : "";

        if (record.getCpuUsage() != null && record.getCpuUsage().compareTo(CPU_THRESHOLD) > 0) {
            createAlert(record.getDeviceId(), record.getDeviceCode(), deviceName,
                    "cpu", 3, record.getCpuUsage() + "%", CPU_THRESHOLD + "%",
                    "CPU usage is over threshold: " + record.getCpuUsage() + "%");
        }
        if (record.getMemoryUsage() != null && record.getMemoryUsage().compareTo(MEMORY_THRESHOLD) > 0) {
            createAlert(record.getDeviceId(), record.getDeviceCode(), deviceName,
                    "memory", 2, record.getMemoryUsage() + "%", MEMORY_THRESHOLD + "%",
                    "Memory usage is over threshold: " + record.getMemoryUsage() + "%");
        }
        if (record.getDiskUsage() != null && record.getDiskUsage().compareTo(DISK_THRESHOLD) > 0) {
            createAlert(record.getDeviceId(), record.getDeviceCode(), deviceName,
                    "disk", 3, record.getDiskUsage() + "%", DISK_THRESHOLD + "%",
                    "Disk usage is over threshold: " + record.getDiskUsage() + "%");
        }
        if (record.getTemperature() != null && record.getTemperature().compareTo(TEMP_THRESHOLD) > 0) {
            createAlert(record.getDeviceId(), record.getDeviceCode(), deviceName,
                    "temperature", 3, record.getTemperature() + " C", TEMP_THRESHOLD + " C",
                    "Temperature is over threshold: " + record.getTemperature() + " C");
        }
        if (record.getNetworkStatus() != null && record.getNetworkStatus() == 0) {
            createAlert(record.getDeviceId(), record.getDeviceCode(), deviceName,
                    "offline", 4, "offline", "online", "Device network is offline");
        }
    }

    @Override
    public void simulateRealtimeStatus() {
        List<DeviceInfo> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<DeviceInfo>().orderByAsc(DeviceInfo::getId));
        if (devices.isEmpty()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (DeviceInfo device : devices) {
            DeviceStatusRecord record = new DeviceStatusRecord();
            record.setDeviceId(device.getId());
            record.setDeviceCode(device.getDeviceCode());

            boolean offline = random.nextInt(100) < 10;
            record.setNetworkStatus(offline ? 0 : 1);
            record.setPowerStatus(offline ? 0 : 1);
            record.setCpuUsage(metric(random, 18, 72, 88, 98, 14));
            record.setMemoryUsage(metric(random, 24, 78, 84, 96, 12));
            record.setDiskUsage(metric(random, 30, 82, 88, 97, 8));
            record.setTemperature(metric(random, 28, 62, 70, 86, 10));
            record.setRunDuration(random.nextLong(60, 24000));
            record.setExtraParams("{\"source\":\"simulator\"}");

            reportStatus(record);
        }
        log.debug("Simulated realtime status for {} devices", devices.size());
    }

    @Override
    public long countUnhandledAlerts() {
        return alertMapper.selectCount(new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 0));
    }

    private int countUnhandledAlerts(Long deviceId) {
        Long count = alertMapper.selectCount(new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getDeviceId, deviceId)
                .eq(DeviceAlert::getStatus, 0));
        return count == null ? 0 : count.intValue();
    }

    private List<RealtimeMonitorVO.AlertItem> getLatestUnhandledAlerts() {
        List<DeviceAlert> alerts = alertMapper.selectList(new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getStatus, 0)
                .orderByDesc(DeviceAlert::getAlertLevel)
                .orderByDesc(DeviceAlert::getCreateTime)
                .last("LIMIT 8"));

        List<RealtimeMonitorVO.AlertItem> items = new ArrayList<>();
        for (DeviceAlert alert : alerts) {
            RealtimeMonitorVO.AlertItem item = new RealtimeMonitorVO.AlertItem();
            item.setId(alert.getId());
            item.setDeviceId(alert.getDeviceId());
            item.setDeviceCode(alert.getDeviceCode());
            item.setDeviceName(alert.getDeviceName());
            item.setAlertType(alert.getAlertType());
            item.setAlertLevel(alert.getAlertLevel());
            item.setAlertMsg(alert.getAlertMsg());
            item.setCreateTime(alert.getCreateTime());
            items.add(item);
        }
        return items;
    }

    private List<RealtimeMonitorVO.TrendPoint> getRecentTrend() {
        List<DeviceStatusRecord> records = statusMapper.selectList(new LambdaQueryWrapper<DeviceStatusRecord>()
                .orderByDesc(DeviceStatusRecord::getRecordTime)
                .last("LIMIT 12"));
        Collections.reverse(records);

        List<RealtimeMonitorVO.TrendPoint> trend = new ArrayList<>();
        for (DeviceStatusRecord record : records) {
            RealtimeMonitorVO.TrendPoint point = new RealtimeMonitorVO.TrendPoint();
            LocalDateTime recordTime = record.getRecordTime();
            point.setLabel(recordTime == null ? "" : recordTime.format(TIME_FORMATTER));
            point.setCpuUsage(record.getCpuUsage());
            point.setMemoryUsage(record.getMemoryUsage());
            point.setTemperature(record.getTemperature());
            trend.add(point);
        }
        return trend;
    }

    private void createAlert(Long deviceId, String deviceCode, String deviceName,
                             String alertType, int alertLevel, String alertValue, String threshold, String msg) {
        Long existing = alertMapper.selectCount(new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getDeviceId, deviceId)
                .eq(DeviceAlert::getAlertType, alertType)
                .eq(DeviceAlert::getStatus, 0));
        if (existing != null && existing > 0) {
            return;
        }

        DeviceAlert alert = new DeviceAlert();
        alert.setDeviceId(deviceId);
        alert.setDeviceCode(deviceCode);
        alert.setDeviceName(deviceName);
        alert.setAlertType(alertType);
        alert.setAlertLevel(alertLevel);
        alert.setAlertValue(alertValue);
        alert.setThreshold(threshold);
        alert.setAlertMsg(msg);
        alert.setStatus(0);
        alertMapper.insert(alert);
    }

    private int calculateHealthScore(RealtimeMonitorVO.DeviceRealtimeItem item, int alertCount) {
        int score = 100;
        if (!Integer.valueOf(1).equals(item.getOnlineStatus()) || Integer.valueOf(0).equals(item.getNetworkStatus())) {
            score -= 35;
        }
        if (item.getStatus() != null && item.getStatus() != 1) {
            score -= 18;
        }
        score -= pressurePenalty(item.getCpuUsage(), 70, 90, 22);
        score -= pressurePenalty(item.getMemoryUsage(), 70, 85, 18);
        score -= pressurePenalty(item.getDiskUsage(), 75, 90, 16);
        score -= pressurePenalty(item.getTemperature(), 60, 70, 24);
        score -= Math.min(24, alertCount * 8);
        return Math.max(0, Math.min(100, score));
    }

    private int pressurePenalty(BigDecimal value, int warning, int critical, int maxPenalty) {
        if (value == null || value.compareTo(BigDecimal.valueOf(warning)) < 0) {
            return 0;
        }
        if (value.compareTo(BigDecimal.valueOf(critical)) >= 0) {
            return maxPenalty;
        }
        BigDecimal range = BigDecimal.valueOf(critical - warning);
        BigDecimal over = value.subtract(BigDecimal.valueOf(warning));
        return over.multiply(BigDecimal.valueOf(maxPenalty))
                .divide(range, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String riskLevel(int healthScore) {
        if (healthScore >= 85) {
            return "normal";
        }
        if (healthScore >= 70) {
            return "watch";
        }
        if (healthScore >= 55) {
            return "warning";
        }
        return "critical";
    }

    private String riskLabel(int healthScore) {
        if (healthScore >= 85) {
            return "健康";
        }
        if (healthScore >= 70) {
            return "关注";
        }
        if (healthScore >= 55) {
            return "预警";
        }
        return "高危";
    }

    private String maintenanceAdvice(RealtimeMonitorVO.DeviceRealtimeItem item, int alertCount) {
        if (!Integer.valueOf(1).equals(item.getOnlineStatus()) || Integer.valueOf(0).equals(item.getNetworkStatus())) {
            return "优先检查供电与网络链路，确认设备是否掉线。";
        }
        if (isAtLeast(item.getTemperature(), TEMP_THRESHOLD)) {
            return "温度偏高，建议检查散热、风扇与设备周边通风。";
        }
        if (isAtLeast(item.getCpuUsage(), CPU_THRESHOLD)) {
            return "CPU 负载过高，建议排查异常进程或高并发任务。";
        }
        if (isAtLeast(item.getMemoryUsage(), MEMORY_THRESHOLD)) {
            return "内存占用偏高，建议重启服务或扩容内存资源。";
        }
        if (isAtLeast(item.getDiskUsage(), DISK_THRESHOLD)) {
            return "磁盘空间紧张，建议清理日志和历史采集数据。";
        }
        if (alertCount > 0) {
            return "存在未处理告警，建议尽快完成工单闭环。";
        }
        if (item.getHealthScore() != null && item.getHealthScore() < 85) {
            return "指标有波动，建议持续观察下一轮采集数据。";
        }
        return "运行平稳，按计划巡检即可。";
    }

    private boolean isAtLeast(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) >= 0;
    }

    private BigDecimal metric(ThreadLocalRandom random,
                              int min,
                              int max,
                              int spikeMin,
                              int spikeMax,
                              int spikeChancePercent) {
        boolean spike = random.nextInt(100) < spikeChancePercent;
        double value = spike
                ? random.nextDouble(spikeMin, spikeMax)
                : random.nextDouble(min, max);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
