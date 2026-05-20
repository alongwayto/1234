package com.campus.equipment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.entity.DeviceAlert;
import com.campus.equipment.entity.DeviceStatusRecord;
import com.campus.equipment.vo.RealtimeMonitorVO;

import java.util.List;

public interface MonitorService {
    void reportStatus(DeviceStatusRecord record);

    List<DeviceStatusRecord> getRecentStatus(Long deviceId, int limit);

    RealtimeMonitorVO getRealtimeMonitor();

    Page<DeviceAlert> pageAlerts(int pageNum, int pageSize, Long deviceId, Integer status, Integer alertLevel);

    void handleAlert(Long alertId, String handler, String remark, Integer status);

    void checkAndGenerateAlerts(DeviceStatusRecord record);

    void simulateRealtimeStatus();

    long countUnhandledAlerts();
}
