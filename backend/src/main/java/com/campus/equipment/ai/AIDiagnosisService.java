package com.campus.equipment.ai;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI 诊断服务 - 基于豆包/火山引擎大模型
 * 用于设备故障诊断和智能维护建议
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIDiagnosisService {

    private final ObjectMapper objectMapper;
    
    @Value("${ai.api.key:}")
    private String apiKey;
    
    @Value("${ai.api.url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String apiUrl;
    
    @Value("${ai.model:doubao-pro}")
    private String model;
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 诊断设备状态问题
     */
    public DiagnosisResult diagnoseDevice(DeviceStatusData data) {
        if (StrUtil.isBlank(apiKey)) {
            return getMockDiagnosis(data);
        }
        
        String prompt = buildDiagnosisPrompt(data);
        String response = callAI(prompt);
        
        return parseDiagnosisResponse(response, data);
    }

    /**
     * 生成维护建议
     */
    public String generateMaintenanceAdvice(DeviceStatusData data) {
        if (StrUtil.isBlank(apiKey)) {
            return generateMockAdvice(data);
        }
        
        String prompt = buildAdvicePrompt(data);
        return callAI(prompt);
    }

    /**
     * 预测设备故障风险
     */
    public RiskPrediction predictFailureRisk(DeviceStatusData data, List<DeviceStatusData> history) {
        if (StrUtil.isBlank(apiKey) || history == null || history.isEmpty()) {
            return calculateLocalRisk(data, history);
        }
        
        String prompt = buildRiskPredictionPrompt(data, history);
        String response = callAI(prompt);
        
        return parseRiskResponse(response, data);
    }

    /**
     * 智能问答
     */
    public String answerQuestion(String question, String context) {
        if (StrUtil.isBlank(apiKey)) {
            return getMockAnswer(question);
        }
        
        String prompt = buildQAPrompt(question, context);
        return callAI(prompt);
    }

    /**
     * 调用 AI 接口
     */
    private String callAI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                "role", "system",
                "content", "你是一位专业的校园设备管理专家，擅长设备故障诊断、维护建议和智能问答。请用专业但易懂的语言回答。"
            ));
            messages.add(Map.of(
                "role", "user", 
                "content", prompt
            ));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(requestBody), JSON);
            
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
            
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI API 调用失败: {}", response);
                    return "AI服务暂时不可用，请稍后重试。";
                }
                
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                
                // 兼容不同 API 响应格式
                JsonNode choices = root.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    return choices.get(0).path("message").path("content").asText();
                }
                
                // 豆包/火山格式
                JsonNode dataNode = root.path("data");
                if (dataNode.has("choices")) {
                    return dataNode.path("choices").get(0).path("message").path("content").asText();
                }
                
                return root.path("content").asText("AI响应格式异常");
            }
        } catch (Exception e) {
            log.error("AI 调用异常", e);
            return "AI服务暂时不可用，请稍后重试。";
        }
    }

    // ==================== Prompt 构建 ====================

    private String buildDiagnosisPrompt(DeviceStatusData data) {
        return String.format("""
            请分析以下设备状态数据，诊断可能存在的问题：
            
            设备信息：
            - 设备编号：%s
            - 设备名称：%s
            - 设备状态：%s
            - CPU使用率：%s%%
            - 内存使用率：%s%%
            - 磁盘使用率：%s%%
            - 温度：%s℃
            - 运行时长：%s小时
            
            请提供：
            1. 问题诊断分析
            2. 可能的原因
            3. 建议的处理措施
            4. 紧急程度评估（低/中/高/紧急）
            """,
            data.getDeviceCode(),
            data.getDeviceName(),
            data.getStatus(),
            data.getCpuUsage(),
            data.getMemoryUsage(),
            data.getDiskUsage(),
            data.getTemperature(),
            data.getRunDuration()
        );
    }

    private String buildAdvicePrompt(DeviceStatusData data) {
        return String.format("""
            基于以下设备状态，请生成专业的维护建议：
            
            设备信息：
            - 设备编号：%s
            - 设备名称：%s
            - CPU使用率：%s%%
            - 内存使用率：%s%%
            - 磁盘使用率：%s%%
            - 温度：%s℃
            - 保修状态：%s
            
            请生成3-5条具体可执行的维护建议，按优先级排序。
            """,
            data.getDeviceCode(),
            data.getDeviceName(),
            data.getCpuUsage(),
            data.getMemoryUsage(),
            data.getDiskUsage(),
            data.getTemperature(),
            data.getWarrantyStatus()
        );
    }

    private String buildRiskPredictionPrompt(DeviceStatusData data, List<DeviceStatusData> history) {
        StringBuilder sb = new StringBuilder("请基于设备历史运行数据预测故障风险：\n\n");
        sb.append("当前状态：\n");
        sb.append(String.format("- CPU：%s%% 内存：%s%% 磁盘：%s%% 温度：%s℃\n\n", 
                data.getCpuUsage(), data.getMemoryUsage(), data.getDiskUsage(), data.getTemperature()));
        
        sb.append("历史趋势（最近").append(Math.min(history.size(), 10)).append("条记录）：\n");
        int count = 0;
        for (DeviceStatusData record : history) {
            if (count++ >= 10) break;
            sb.append(String.format("- 时间：%s | CPU：%s%% | 温度：%s℃\n", 
                    record.getRecordTime(), record.getCpuUsage(), record.getTemperature()));
        }
        
        sb.append("\n请分析并给出：\n1. 故障概率（0-100%）\n2. 可能故障类型\n3. 预计发生时间\n4. 预防措施");
        
        return sb.toString();
    }

    private String buildQAPrompt(String question, String context) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(context)) {
            sb.append("相关上下文信息：\n").append(context).append("\n\n");
        }
        sb.append("用户问题：").append(question);
        return sb.toString();
    }

    // ==================== 响应解析 ====================

    private DiagnosisResult parseDiagnosisResponse(String response, DeviceStatusData data) {
        DiagnosisResult result = new DiagnosisResult();
        result.setDeviceCode(data.getDeviceCode());
        result.setDeviceName(data.getDeviceName());
        result.setRawResponse(response);
        result.setTimestamp(new Date());
        
        // 解析紧急程度
        if (response.contains("紧急") || response.contains("critical")) {
            result.setUrgencyLevel(4);
        } else if (response.contains("高") || response.contains("high")) {
            result.setUrgencyLevel(3);
        } else if (response.contains("中") || response.contains("medium")) {
            result.setUrgencyLevel(2);
        } else {
            result.setUrgencyLevel(1);
        }
        
        // 生成建议
        result.setSuggestions(extractSuggestions(response));
        
        return result;
    }

    private RiskPrediction parseRiskResponse(String response, DeviceStatusData data) {
        RiskPrediction prediction = new RiskPrediction();
        prediction.setDeviceCode(data.getDeviceCode());
        prediction.setDeviceName(data.getDeviceName());
        
        // 提取风险概率
        if (response.contains("故障概率")) {
            try {
                String probStr = response.replaceAll(".*故障概率[：:]*\\s*(\\d+).*", "$1");
                prediction.setFailureProbability(Integer.parseInt(probStr));
            } catch (Exception e) {
                prediction.setFailureProbability(calculateLocalRiskValue(data));
            }
        } else {
            prediction.setFailureProbability(calculateLocalRiskValue(data));
        }
        
        prediction.setAnalysis(response);
        prediction.setTimestamp(new Date());
        
        return prediction;
    }

    private List<String> extractSuggestions(String response) {
        List<String> suggestions = new ArrayList<>();
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.contains("建议") || line.contains("措施") || line.matches("^\\d+[.、].*")) {
                suggestions.add(line.trim());
            }
        }
        return suggestions;
    }

    // ==================== 本地备用方案 ====================

    private DiagnosisResult getMockDiagnosis(DeviceStatusData data) {
        DiagnosisResult result = new DiagnosisResult();
        result.setDeviceCode(data.getDeviceCode());
        result.setDeviceName(data.getDeviceName());
        result.setRawResponse("【模拟诊断结果】AI服务未配置，使用本地诊断逻辑。");
        result.setTimestamp(new Date());
        
        // 本地简单诊断逻辑
        List<String> suggestions = new ArrayList<>();
        int urgency = 1;
        
        if (data.getCpuUsage() != null && data.getCpuUsage() > 90) {
            suggestions.add("CPU使用率过高，建议检查是否有异常进程运行");
            urgency = Math.max(urgency, 3);
        }
        if (data.getMemoryUsage() != null && data.getMemoryUsage() > 85) {
            suggestions.add("内存使用率偏高，建议清理不必要的程序");
            urgency = Math.max(urgency, 2);
        }
        if (data.getDiskUsage() != null && data.getDiskUsage() > 90) {
            suggestions.add("磁盘空间不足，建议清理或扩展存储");
            urgency = Math.max(urgency, 3);
        }
        if (data.getTemperature() != null && data.getTemperature() > 70) {
            suggestions.add("设备温度过高，建议检查散热系统");
            urgency = Math.max(urgency, 4);
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("设备运行正常，建议保持当前维护状态");
        }
        
        result.setUrgencyLevel(urgency);
        result.setSuggestions(suggestions);
        
        return result;
    }

    private String generateMockAdvice(DeviceStatusData data) {
        StringBuilder sb = new StringBuilder("【维护建议】\n");
        
        if (data.getCpuUsage() != null && data.getCpuUsage() > 70) {
            sb.append("1. CPU使用率偏高，建议安排进行系统优化\n");
        }
        if (data.getMemoryUsage() != null && data.getMemoryUsage() > 70) {
            sb.append("2. 建议定期清理内存，优化启动项\n");
        }
        if (data.getTemperature() != null && data.getTemperature() > 60) {
            sb.append("3. 温度较高，建议检查散热设备\n");
        }
        
        sb.append("4. 建议每周进行一次例行巡检\n");
        sb.append("5. 关注设备保修期，及时处理潜在问题");
        
        return sb.toString();
    }

    private RiskPrediction calculateLocalRisk(DeviceStatusData data, List<DeviceStatusData> history) {
        RiskPrediction prediction = new RiskPrediction();
        prediction.setDeviceCode(data.getDeviceCode());
        prediction.setDeviceName(data.getDeviceName());
        prediction.setFailureProbability(calculateLocalRiskValue(data));
        prediction.setTimestamp(new Date());
        
        if (prediction.getFailureProbability() > 70) {
            prediction.setAnalysis("根据本地算法分析，故障风险较高，建议尽快检查");
        } else if (prediction.getFailureProbability() > 40) {
            prediction.setAnalysis("存在一定风险，建议关注设备状态变化");
        } else {
            prediction.setAnalysis("设备运行稳定，故障风险较低");
        }
        
        return prediction;
    }

    private int calculateLocalRiskValue(DeviceStatusData data) {
        int risk = 0;
        
        if (data.getCpuUsage() != null && data.getCpuUsage() > 85) risk += 25;
        if (data.getMemoryUsage() != null && data.getMemoryUsage() > 80) risk += 20;
        if (data.getDiskUsage() != null && data.getDiskUsage() > 85) risk += 25;
        if (data.getTemperature() != null && data.getTemperature() > 65) risk += 30;
        
        return Math.min(100, risk);
    }

    private String getMockAnswer(String question) {
        if (question.contains("故障") || question.contains("维修")) {
            return "根据您的问题，建议您：\n1. 首先检查设备电源和连接线\n2. 查看设备状态指示灯\n3. 如无法解决，请联系维护人员或提交维修工单";
        }
        if (question.contains("维护") || question.contains("保养")) {
            return "设备维护建议：\n1. 定期清洁设备表面\n2. 保持良好通风环境\n3. 及时更新系统和软件\n4. 记录设备运行状态";
        }
        return "您好！我是智能设备助手。请告诉我具体的设备问题，我会尽力为您提供帮助。";
    }

    // ==================== 内部类 ====================

    @Data
    public static class DeviceStatusData {
        private String deviceCode;
        private String deviceName;
        private String status;
        private Double cpuUsage;
        private Double memoryUsage;
        private Double diskUsage;
        private Double temperature;
        private Long runDuration;
        private String warrantyStatus;
        private String recordTime;
    }

    @Data
    public static class DiagnosisResult {
        private String deviceCode;
        private String deviceName;
        private int urgencyLevel; // 1-4
        private List<String> suggestions;
        private String rawResponse;
        private Date timestamp;
    }

    @Data
    public static class RiskPrediction {
        private String deviceCode;
        private String deviceName;
        private int failureProbability; // 0-100
        private String analysis;
        private Date timestamp;
    }
}
