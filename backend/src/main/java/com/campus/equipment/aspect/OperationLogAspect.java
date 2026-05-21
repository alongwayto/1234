package com.campus.equipment.aspect;

import com.campus.equipment.entity.SysOperationLog;
import com.campus.equipment.service.SysOperationLogService;
import com.campus.equipment.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 自动记录Controller方法的操作日志
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    /**
     * 定义切点：所有Controller的方法
     */
    @Pointcut("execution(* com.campus.equipment.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog log = new SysOperationLog();
        
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            
            if (request != null) {
                log.setRequestUrl(request.getRequestURI());
                log.setRequestMethod(request.getMethod());
                log.setIp(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setLocation(getLocationFromIp(log.getIp()));
            }
            
            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 设置模块和描述（从注解或方法名获取）
            String className = joinPoint.getTarget().getClass().getSimpleName();
            log.setModule(className.replace("Controller", ""));
            log.setDescription(getMethodDescription(method, joinPoint.getArgs()));
            
            // 获取请求参数
            log.setRequestParams(getRequestParams(method, joinPoint.getArgs()));
            
            // 获取用户信息
            try {
                Long userId = SecurityUtils.getUserId();
                String username = SecurityUtils.getUsername();
                String realName = SecurityUtils.getRealName();
                
                log.setUserId(userId);
                log.setUsername(username);
                log.setRealName(realName);
            } catch (Exception e) {
                // 未登录情况
                log.setUserId(0L);
                log.setUsername("anonymous");
            }
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功
            log.setSuccess(1);
            log.setResponseResult(getResultSummary(result));
            log.setOperationType(determineOperationType(method.getName()));
            
            // 异步保存日志
            saveLogAsync(log);
            
            return result;
            
        } catch (Exception e) {
            // 记录失败
            log.setSuccess(0);
            log.setErrorMsg(e.getMessage());
            log.setOperationType("ERROR");
            
            // 异步保存日志
            saveLogAsync(log);
            
            throw e;
        } finally {
            log.setDuration(System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 异步保存日志
     */
    @Async
    public void saveLogAsync(SysOperationLog log) {
        try {
            log.setCreateTime(LocalDateTime.now());
            operationLogService.save(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 获取方法描述
     */
    private String getMethodDescription(Method method, Object[] args) {
        // 从方法名生成描述
        String methodName = method.getName();
        
        // 常见CRUD操作映射
        if (methodName.startsWith("get") || methodName.startsWith("query") || methodName.startsWith("find")) {
            return "查询数据";
        } else if (methodName.startsWith("add") || methodName.startsWith("create") || methodName.startsWith("insert")) {
            return "新增数据";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return "更新数据";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "删除数据";
        } else if (methodName.startsWith("export")) {
            return "导出数据";
        } else if (methodName.startsWith("import")) {
            return "导入数据";
        } else if (methodName.startsWith("login")) {
            return "用户登录";
        } else if (methodName.startsWith("logout")) {
            return "用户登出";
        }
        
        return methodName;
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(Method method, Object[] args) {
        try {
            Parameter[] parameters = method.getParameters();
            Map<String, Object> params = new HashMap<>();
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Object arg = args[i];
                
                // 跳过HttpServletRequest和Response
                if (arg == null || arg instanceof jakarta.servlet.http.HttpServletRequest 
                        || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                    continue;
                }
                
                // 获取参数名
                String paramName = param.getName();
                
                // 检查是否有RequestParam注解
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                if (requestParam != null && !requestParam.value().isEmpty()) {
                    paramName = requestParam.value();
                }
                
                // 检查是否有RequestBody注解
                if (param.isAnnotationPresent(RequestBody.class) && arg != null) {
                    params.put(paramName, arg);
                } else if (arg != null) {
                    params.put(paramName, arg.toString());
                }
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 获取结果摘要
     */
    private String getResultSummary(Object result) {
        if (result == null) {
            return "void";
        }
        return result.getClass().getSimpleName();
    }

    /**
     * 确定操作类型
     */
    private String determineOperationType(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("query") || methodName.startsWith("list") || methodName.startsWith("page")) {
            return "QUERY";
        } else if (methodName.startsWith("add") || methodName.startsWith("create") || methodName.startsWith("insert") || methodName.startsWith("save")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("export")) {
            return "EXPORT";
        } else if (methodName.startsWith("import")) {
            return "IMPORT";
        } else if (methodName.startsWith("login")) {
            return "LOGIN";
        } else if (methodName.startsWith("logout")) {
            return "LOGOUT";
        }
        return "OTHER";
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 根据IP获取位置
     * 实际实现可使用IP库或第三方服务
     */
    private String getLocationFromIp(String ip) {
        if (ip == null || ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "本地";
        }
        return "未知位置";
    }
}
