package com.campus.equipment.controller;

import com.campus.equipment.ai.AIDiagnosisService;
import com.campus.equipment.ai.AIDiagnosisService.DiagnosisResult;
import com.campus.equipment.ai.AIDiagnosisService.DeviceStatusData;
import com.campus.equipment.ai.AIDiagnosisService.RiskPrediction;
import com.campus.equipment.common.Result;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.DeviceStatusRecord;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.DeviceStatusRecordMapper;
import com.campus.equipment.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 智能诊断控制器
 * 提供设备故障诊断、维护建议、风险预测等功能
 */
@Tag(name = "AI智能诊断")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIDiagnosisController {

    private final AIDiagnosisService aiDiagnosisService;
    private final DeviceService deviceService;
    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceStatusRecordMapper statusRecordMapper;

    @Operation(summary = "诊断设备状态")
    @PostMapping("/diagnose/{deviceId}")
    public Result<DiagnosisResult> diagnoseDevice(@PathVariable Long deviceId) {
        DeviceInfo device = deviceService.getDeviceById(deviceId);
        if (device == null) {
            return Result.fail("设备不存在");
        }
        
        DeviceStatusData data = buildDeviceStatusData(device);
        DiagnosisResult result = aiDiagnosisService.diagnoseDevice(data);
        
        return Result.success(result);
    }

    @Operation(summary = "批量诊断设备")
    @PostMapping("/diagnose/batch")
    public Result<List<DiagnosisResult>> batchDiagnose(@RequestBody List<Long> deviceIds) {
        List<DiagnosisResult> results = new ArrayList<>();
        
        for (Long deviceId : deviceIds) {
            try {
                DeviceInfo device = deviceService.getDeviceById(deviceId);
                if (device != null) {
                    DeviceStatusData data = buildDeviceStatusData(device);
                    DiagnosisResult result = aiDiagnosisService.diagnoseDevice(data);
                    results.add(result);
                }
            } catch (Exception e) {
                // 单个设备失败不影响其他设备
            }
        }
        
        return Result.success(results);
    }

    @Operation(summary = "生成维护建议")
    @PostMapping("/advice/{deviceId}")
    public Result<String> generateAdvice(@PathVariable Long deviceId) {
        DeviceInfo device = deviceService.getDeviceById(deviceId);
        if (device == null) {
            return Result.fail("设备不存在");
        }
        
        DeviceStatusData data = buildDeviceStatusData(device);
        String advice = aiDiagnosisService.generateMaintenanceAdvice(data);
        
        return Result.success(advice);
    }

    @Operation(summary = "预测故障风险")
    @GetMapping("/risk/{deviceId}")
    public Result<RiskPrediction> predictRisk(@PathVariable Long deviceId) {
        DeviceInfo device = deviceService.getDeviceById(deviceId);
        if (device == null) {
            return Result.fail("设备不存在");
        }
        
        DeviceStatusData currentData = buildDeviceStatusData(device);
        
        // 获取历史数据
        List<DeviceStatusRecord> history = statusRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceStatusRecord>()
                .eq("device_id", deviceId)
                .orderByDesc("record_time")
                .last("LIMIT 50")
        );
        
        List<DeviceStatusData> historyData = new ArrayList<>();
        for (DeviceStatusRecord record : history) {
            DeviceStatusData data = new DeviceStatusData();
            data.setDeviceCode(device.getDeviceCode());
            data.setDeviceName(device.getDeviceName());
            data.setCpuUsage(record.getCpuUsage() != null ? record.getCpuUsage().doubleValue() : null);
            data.setMemoryUsage(record.getMemoryUsage() != null ? record.getMemoryUsage().doubleValue() : null);
            data.setTemperature(record.getTemperature() != null ? record.getTemperature().doubleValue() : null);
            data.setRecordTime(record.getRecordTime() != null ? record.getRecordTime().toString() : null);
            historyData.add(data);
        }
        
        RiskPrediction prediction = aiDiagnosisService.predictFailureRisk(currentData, historyData);
        
        return Result.success(prediction);
    }

    @Operation(summary = "AI智能问答")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        String context = request.getContext() != null ? request.getContext() : "";
        String answer = aiDiagnosisService.answerQuestion(request.getQuestion(), context);
        
        return Result.success(answer);
    }

    @Operation(summary = "分析故障报告")
    @PostMapping("/analyze-fault")
    public Result<DiagnosisResult> analyzeFault(@RequestBody FaultAnalysisRequest request) {
        DeviceStatusData data = new DeviceStatusData();
        data.setDeviceCode(request.getDeviceCode());
        data.setDeviceName(request.getDeviceName());
        data.setStatus(request.getFaultType());
        
        DiagnosisResult result = aiDiagnosisService.diagnoseDevice(data);
        return Result.success(result);
    }

    private DeviceStatusData buildDeviceStatusData(DeviceInfo device) {
        DeviceStatusData data = new DeviceStatusData();
        data.setDeviceCode(device.getDeviceCode());
        data.setDeviceName(device.getDeviceName());
        data.setStatus(getStatusText(device.getStatus()));
        
        // 获取最新状态记录
        List<DeviceStatusRecord> records = statusRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceStatusRecord>()
                .eq("device_id", device.getId())
                .orderByDesc("record_time")
                .last("LIMIT 1")
        );
        
        if (!records.isEmpty()) {
            DeviceStatusRecord record = records.get(0);
            data.setCpuUsage(record.getCpuUsage() != null ? record.getCpuUsage().doubleValue() : null);
            data.setMemoryUsage(record.getMemoryUsage() != null ? record.getMemoryUsage().doubleValue() : null);
            data.setDiskUsage(record.getDiskUsage() != null ? record.getDiskUsage().doubleValue() : null);
            data.setTemperature(record.getTemperature() != null ? record.getTemperature().doubleValue() : null);
            data.setRunDuration(record.getRunDuration());
        }
        
        // 检查保修状态
        if (device.getWarrantyDate() != null) {
            if (device.getWarrantyDate().isBefore(java.time.LocalDate.now())) {
                data.setWarrantyStatus("已过保");
            } else {
                data.setWarrantyStatus("在保");
            }
        } else {
            data.setWarrantyStatus("未知");
        }
        
        return data;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "停用";
            case 1: return "正常";
            case 2: return "维修中";
            case 3: return "报废";
            default: return "未知";
        }
    }

    @Data
    public static class ChatRequest {
        private String question;
        private String context;
    }

    @Data
    public static class FaultAnalysisRequest {
        private String deviceCode;
        private String deviceName;
        private String faultType;
        private String faultDescription;
    }
}
