package com.campus.equipment.aspect;

import com.campus.equipment.entity.SysOperationLog;
import com.campus.equipment.mapper.SysOperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.campus.equipment.aspect.OperationLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog opLog = new SysOperationLog();
        opLog.setCreateTime(LocalDateTime.now());

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        opLog.setModule(annotation.module());
        opLog.setOperation(annotation.operation());
        opLog.setMethod(point.getTarget().getClass().getName() + "." + method.getName());

        // 获取请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            opLog.setRequestUrl(request.getRequestURI());
            opLog.setRequestMethod(request.getMethod());
            opLog.setIp(getClientIp(request));
        }

        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            opLog.setUsername(auth.getName());
        }

        // 记录请求参数
        if (annotation.saveParams()) {
            try {
                opLog.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
            } catch (Exception ignored) {}
        }

        Object result = null;
        try {
            result = point.proceed();
            opLog.setStatus(1);
            if (annotation.saveResult() && result != null) {
                try {
                    opLog.setResponseData(objectMapper.writeValueAsString(result));
                } catch (Exception ignored) {}
            }
        } catch (Throwable e) {
            opLog.setStatus(0);
            opLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            try {
                logMapper.insert(opLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
