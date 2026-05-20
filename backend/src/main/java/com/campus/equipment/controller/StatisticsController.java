package com.campus.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.DeviceAlert;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.FaultWorkOrder;
import com.campus.equipment.entity.MaintenanceCost;
import com.campus.equipment.mapper.DeviceAlertMapper;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.FaultReportMapper;
import com.campus.equipment.mapper.FaultWorkOrderMapper;
import com.campus.equipment.mapper.MaintenanceCostMapper;
import com.campus.equipment.service.MonitorService;
import com.campus.equipment.vo.RealtimeMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Statistics")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final DeviceInfoMapper deviceMapper;
    private final FaultReportMapper faultMapper;
    private final FaultWorkOrderMapper workOrderMapper;
    private final MaintenanceCostMapper costMapper;
    private final DeviceAlertMapper alertMapper;
    private final MonitorService monitorService;

    @Operation(summary = "Homepage overview")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        data.put("totalDevices", countDevices(null, null));
        data.put("onlineDevices", countDevices(null, 1));
        data.put("faultDevices", countDevices(2, null));
        data.put("unhandledAlerts", countUnhandledAlerts());
        data.put("monthFaults", faultMapper.selectCount(new LambdaQueryWrapper<FaultReport>()
                .ge(FaultReport::getCreateTime, firstDay.atStartOfDay())
                .eq(FaultReport::getDeleted, 0)));
        data.put("pendingOrders", workOrderMapper.selectCount(new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, 0)
                .eq(FaultWorkOrder::getDeleted, 0)));
        return Result.success(data);
    }

    @Operation(summary = "Operations dashboard")
    @GetMapping("/operations")
    public Result<Map<String, Object>> operations() {
        Map<String, Object> data = new LinkedHashMap<>();
        RealtimeMonitorVO realtime = monitorService.getRealtimeMonitor();
        Map<String, Object> overview = overview().getData();

        long totalOrders = workOrderMapper.selectCount(new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getDeleted, 0));
        long pendingOrders = countWorkOrders(0);
        long processingOrders = countWorkOrders(1);
        long finishedOrders = countWorkOrders(2) + countWorkOrders(3);

        BigDecimal completionRate = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(finishedOrders * 100.0 / totalOrders).setScale(1, RoundingMode.HALF_UP);

        Map<String, Object> workOrders = new LinkedHashMap<>();
        workOrders.put("total", totalOrders);
        workOrders.put("pending", pendingOrders);
        workOrders.put("processing", processingOrders);
        workOrders.put("finished", finishedOrders);
        workOrders.put("completionRate", completionRate);

        Map<String, Object> alerts = new LinkedHashMap<>();
        alerts.put("unhandled", countUnhandledAlerts());
        alerts.put("critical", countUnhandledAlertsByLevel(4));
        alerts.put("high", countUnhandledAlertsByLevel(3));
        alerts.put("medium", countUnhandledAlertsByLevel(2));

        data.put("overview", overview);
        data.put("onlineRate", realtime.getOnlineRate());
        data.put("averageHealthScore", realtime.getAverageHealthScore());
        data.put("healthDistribution", buildHealthDistribution(realtime.getDevices()));
        data.put("riskDevices", realtime.getRiskDevices());
        data.put("recentAlerts", realtime.getAlerts());
        data.put("workOrders", workOrders);
        data.put("alerts", alerts);
        data.put("actionItems", buildActionItems(realtime, pendingOrders, alerts));
        data.put("faultTrend", faultTrend().getData());
        data.put("costMonthly", costMonthly(6, null, null).getData());
        return Result.success(data);
    }

    @Operation(summary = "Device status distribution")
    @GetMapping("/device-status-dist")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER','statistics:usage','statistics:fault')")
    public Result<List<Map<String, Object>>> deviceStatusDist() {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] statusNames = {"停用", "正常", "维修中", "报废"};
        for (int i = 0; i < 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", countDevices(i, null));
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "7-day fault trend")
    @GetMapping("/fault-trend")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER','statistics:fault')")
    public Result<Map<String, Object>> faultTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            long count = faultMapper.selectCount(new LambdaQueryWrapper<FaultReport>()
                    .ge(FaultReport::getCreateTime, date.atStartOfDay())
                    .lt(FaultReport::getCreateTime, date.plusDays(1).atStartOfDay())
                    .eq(FaultReport::getDeleted, 0));
            counts.add(count);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("counts", counts);
        return Result.success(data);
    }

    @Operation(summary = "Fault type distribution")
    @GetMapping("/fault-type-dist")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER','statistics:fault')")
    public Result<List<Map<String, Object>>> faultTypeDist() {
        List<FaultReport> faults = faultMapper.selectList(new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getDeleted, 0)
                .isNotNull(FaultReport::getFaultType));
        Map<String, Long> grouped = faults.stream()
                .collect(Collectors.groupingBy(f -> f.getFaultType() != null ? f.getFaultType() : "其他", Collectors.counting()));
        return Result.success(grouped.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Monthly maintenance cost")
    @GetMapping("/cost-monthly")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER','statistics:cost')")
    public Result<Map<String, Object>> costMonthly(@RequestParam(defaultValue = "6") int months,
                                                   @RequestParam(required = false) String startMonth,
                                                   @RequestParam(required = false) String endMonth) {
        if (hasText(startMonth) && hasText(endMonth)) {
            YearMonth start = YearMonth.parse(startMonth);
            YearMonth end = YearMonth.parse(endMonth);
            if (end.isBefore(start)) {
                YearMonth temp = start;
                start = end;
                end = temp;
            }
            if (ChronoUnit.MONTHS.between(start, end) > 119) {
                start = end.minusMonths(119);
            }
            return Result.success(buildCostMonthly(start, end));
        }

        int safeMonths = Math.max(1, Math.min(months, 120));
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(safeMonths - 1L);
        return Result.success(buildCostMonthly(start, end));
    }

    private Map<String, Object> buildCostMonthly(YearMonth startMonth, YearMonth endMonth) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> costs = new ArrayList<>();
        YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            LocalDate start = cursor.atDay(1);
            LocalDate end = cursor.plusMonths(1).atDay(1);
            labels.add(cursor.toString());
            List<MaintenanceCost> list = costMapper.selectList(new LambdaQueryWrapper<MaintenanceCost>()
                    .ge(MaintenanceCost::getCostDate, start)
                    .lt(MaintenanceCost::getCostDate, end)
                    .eq(MaintenanceCost::getDeleted, 0));
            BigDecimal total = list.stream()
                    .map(MaintenanceCost::getCostAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            costs.add(total);
            cursor = cursor.plusMonths(1);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("labels", labels);
        data.put("costs", costs);
        return data;
    }

    @Operation(summary = "Device usage")
    @GetMapping("/device-usage")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER','statistics:usage')")
    public Result<List<Map<String, Object>>> deviceUsage() {
        List<DeviceInfo> devices = deviceMapper.selectList(new LambdaQueryWrapper<DeviceInfo>()
                .eq(DeviceInfo::getDeleted, 0)
                .last("LIMIT 10"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceInfo device : devices) {
            Map<String, Object> item = new HashMap<>();
            item.put("deviceName", device.getDeviceName());
            item.put("onlineStatus", device.getOnlineStatus());
            item.put("usageRate", Integer.valueOf(1).equals(device.getOnlineStatus()) ? 90 : 0);
            result.add(item);
        }
        return Result.success(result);
    }

    @Operation(summary = "Top fault devices")
    @GetMapping("/fault-top-devices")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MAINTAINER','statistics:fault','statistics:usage')")
    public Result<List<Map<String, Object>>> faultTopDevices() {
        List<FaultReport> faults = faultMapper.selectList(new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getDeleted, 0));
        Map<String, Long> grouped = faults.stream()
                .collect(Collectors.groupingBy(f -> f.getDeviceName() != null ? f.getDeviceName() : "未知设备", Collectors.counting()));
        return Result.success(grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList()));
    }

    private long countDevices(Integer status, Integer onlineStatus) {
        return deviceMapper.selectCount(new LambdaQueryWrapper<DeviceInfo>()
                .eq(status != null, DeviceInfo::getStatus, status)
                .eq(onlineStatus != null, DeviceInfo::getOnlineStatus, onlineStatus)
                .eq(DeviceInfo::getDeleted, 0));
    }

    private long countWorkOrders(Integer status) {
        return workOrderMapper.selectCount(new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, status)
                .eq(FaultWorkOrder::getDeleted, 0));
    }

    private long countUnhandledAlerts() {
        return alertMapper.selectCount(new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 0));
    }

    private long countUnhandledAlertsByLevel(Integer level) {
        return alertMapper.selectCount(new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getStatus, 0)
                .eq(DeviceAlert::getAlertLevel, level));
    }

    private List<Map<String, Object>> buildHealthDistribution(List<RealtimeMonitorVO.DeviceRealtimeItem> devices) {
        long healthy = devices.stream().filter(d -> scoreBetween(d, 85, 101)).count();
        long watch = devices.stream().filter(d -> scoreBetween(d, 70, 85)).count();
        long warning = devices.stream().filter(d -> scoreBetween(d, 55, 70)).count();
        long critical = devices.stream().filter(d -> scoreBetween(d, 0, 55)).count();
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(namedValue("健康", healthy));
        result.add(namedValue("关注", watch));
        result.add(namedValue("预警", warning));
        result.add(namedValue("高危", critical));
        return result;
    }

    private boolean scoreBetween(RealtimeMonitorVO.DeviceRealtimeItem item, int min, int max) {
        Integer score = item.getHealthScore();
        return score != null && score >= min && score < max;
    }

    private Map<String, Object> namedValue(String name, Object value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private List<String> buildActionItems(RealtimeMonitorVO realtime, long pendingOrders, Map<String, Object> alerts) {
        List<String> items = new ArrayList<>();
        if (toLong(alerts.get("critical")) > 0) {
            items.add("存在紧急告警，建议优先派单处理离线或高温设备。");
        }
        if (!realtime.getRiskDevices().isEmpty()) {
            items.add("风险设备 Top 5 已生成，建议安排维护人员巡检。");
        }
        if (pendingOrders > 0) {
            items.add("存在待处理工单，建议完成派单并跟踪处理进度。");
        }
        if (realtime.getAverageHealthScore().compareTo(BigDecimal.valueOf(75)) < 0) {
            items.add("平均健康度偏低，建议排查高负载设备和历史告警。");
        }
        if (items.isEmpty()) {
            items.add("系统运行平稳，建议保持日常巡检和数据备份。");
        }
        return items;
    }

    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
