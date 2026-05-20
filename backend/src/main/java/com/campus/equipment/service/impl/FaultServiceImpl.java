package com.campus.equipment.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.dto.FaultReportDTO;
import com.campus.equipment.dto.WorkOrderAssignDTO;
import com.campus.equipment.dto.WorkOrderHandleDTO;
import com.campus.equipment.dto.WorkOrderImportDTO;
import com.campus.equipment.entity.*;
import com.campus.equipment.exception.BusinessException;
import com.campus.equipment.mapper.*;
import com.campus.equipment.service.FaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FaultServiceImpl implements FaultService {

    private final FaultReportMapper faultMapper;
    private final FaultWorkOrderMapper workOrderMapper;
    private final DeviceInfoMapper deviceMapper;
    private final SysUserMapper userMapper;
    private final MaintenanceCostMapper costMapper;

    @Override
    public Page<FaultReport> pageFaults(int pageNum, int pageSize, Long deviceId, Integer status, Integer faultLevel) {
        Page<FaultReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FaultReport> wrapper = new LambdaQueryWrapper<FaultReport>()
                .eq(deviceId != null, FaultReport::getDeviceId, deviceId)
                .eq(status != null, FaultReport::getStatus, status)
                .eq(faultLevel != null, FaultReport::getFaultLevel, faultLevel)
                .eq(FaultReport::getDeleted, 0)
                .orderByDesc(FaultReport::getCreateTime);
        return faultMapper.selectPage(page, wrapper);
    }

    @Override
    public FaultReport getFaultById(Long id) {
        FaultReport fault = faultMapper.selectById(id);
        if (fault == null) throw new BusinessException("故障记录不存在");
        return fault;
    }

    @Override
    @Transactional
    public FaultReport createFault(FaultReportDTO dto, String reporterName, Long reporterId) {
        DeviceInfo device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) throw new BusinessException("设备不存在");

        FaultReport fault = new FaultReport();
        BeanUtils.copyProperties(dto, fault);
        fault.setDeviceCode(device.getDeviceCode());
        fault.setDeviceName(device.getDeviceName());
        fault.setReporterId(reporterId);
        fault.setReporterName(reporterName);
        fault.setReportTime(LocalDateTime.now());
        fault.setStatus(0);
        // 生成故障单号
        fault.setFaultNo("FLT-" + DateUtil.format(new Date(), "yyyyMMdd") + "-" + System.currentTimeMillis() % 10000);
        faultMapper.insert(fault);
        return fault;
    }

    @Override
    @Transactional
    public FaultWorkOrder assignWorkOrder(WorkOrderAssignDTO dto, String assignerName, Long assignerId) {
        FaultReport fault = faultMapper.selectById(dto.getFaultId());
        if (fault == null) throw new BusinessException("故障记录不存在");

        SysUser assignee = userMapper.selectById(dto.getAssigneeId());
        if (assignee == null) throw new BusinessException("维护员不存在");

        FaultWorkOrder order = new FaultWorkOrder();
        order.setOrderNo("WO-" + DateUtil.format(new Date(), "yyyyMMdd") + "-" + System.currentTimeMillis() % 10000);
        order.setFaultId(dto.getFaultId());
        order.setAssigneeId(dto.getAssigneeId());
        order.setAssigneeName(assignee.getRealName() != null ? assignee.getRealName() : assignee.getUsername());
        order.setAssignTime(LocalDateTime.now());
        order.setAssignerId(assignerId);
        order.setAssignerName(assignerName);
        order.setExpectedTime(dto.getExpectedTime());
        order.setStatus(0);
        workOrderMapper.insert(order);

        // 更新故障状态为已派单
        fault.setStatus(1);
        faultMapper.updateById(fault);
        return order;
    }

    @Override
    @Transactional
    public FaultWorkOrder importHistoricalWorkOrder(WorkOrderImportDTO dto, String operatorName) {
        DeviceInfo device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) throw new BusinessException("设备不存在");
        if (dto.getActualEnd() == null) throw new BusinessException("实际完成时间不能为空");

        LocalDateTime reportTime = dto.getReportTime() != null ? dto.getReportTime() : dto.getActualEnd();
        LocalDateTime assignTime = dto.getAssignTime() != null ? dto.getAssignTime() : reportTime;

        FaultReport fault = new FaultReport();
        fault.setFaultNo(resolveUniqueFaultNo(dto.getFaultNo(), reportTime));
        fault.setDeviceId(device.getId());
        fault.setDeviceCode(device.getDeviceCode());
        fault.setDeviceName(device.getDeviceName());
        fault.setFaultType(hasText(dto.getFaultType()) ? dto.getFaultType().trim() : "历史导入");
        fault.setFaultLevel(dto.getFaultLevel() != null ? dto.getFaultLevel() : 2);
        fault.setFaultDesc(hasText(dto.getFaultDesc()) ? dto.getFaultDesc().trim() : "历史工单导入");
        fault.setReporterName(hasText(dto.getReporterName()) ? dto.getReporterName().trim() : operatorName);
        fault.setReportTime(reportTime);
        fault.setStatus(3);
        fault.setCreateTime(reportTime);
        fault.setUpdateTime(dto.getActualEnd());
        faultMapper.insert(fault);

        FaultWorkOrder order = new FaultWorkOrder();
        order.setOrderNo(resolveUniqueOrderNo(dto.getOrderNo(), assignTime));
        order.setFaultId(fault.getId());
        order.setAssigneeId(dto.getAssigneeId());
        order.setAssigneeName(resolveAssigneeName(dto));
        order.setAssignTime(assignTime);
        order.setAssignerName(hasText(dto.getAssignerName()) ? dto.getAssignerName().trim() : operatorName);
        order.setExpectedTime(dto.getExpectedTime());
        order.setActualStart(dto.getActualStart());
        order.setActualEnd(dto.getActualEnd());
        order.setHandleDesc(dto.getHandleDesc());
        order.setHandleResult(dto.getHandleResult() != null ? dto.getHandleResult() : 1);
        order.setCost(dto.getCost() != null ? dto.getCost() : BigDecimal.ZERO);
        order.setPartsReplaced(dto.getPartsReplaced());
        order.setStatus(2);
        order.setCreateTime(assignTime);
        order.setUpdateTime(dto.getActualEnd());
        workOrderMapper.insert(order);

        syncMaintenanceCost(order, fault);
        return order;
    }

    @Override
    public void startHandle(Long orderId) {
        FaultWorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("工单不存在");
        order.setStatus(1);
        order.setActualStart(LocalDateTime.now());
        workOrderMapper.updateById(order);
        // 更新故障状态为处理中
        FaultReport fault = faultMapper.selectById(order.getFaultId());
        if (fault != null) { fault.setStatus(2); faultMapper.updateById(fault); }
    }

    @Override
    @Transactional
    public void completeHandle(WorkOrderHandleDTO dto) {
        FaultWorkOrder order = workOrderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("工单不存在");
        order.setHandleDesc(dto.getHandleDesc());
        order.setHandleResult(dto.getHandleResult());
        order.setCost(dto.getCost());
        order.setPartsReplaced(dto.getPartsReplaced());
        order.setStatus(2);
        order.setActualEnd(LocalDateTime.now());
        workOrderMapper.updateById(order);
        // 更新故障状态为已完成
        FaultReport fault = faultMapper.selectById(order.getFaultId());
        if (fault != null) { fault.setStatus(3); faultMapper.updateById(fault); }
        syncMaintenanceCost(order, fault);
    }

    @Override
    public void archiveFault(Long faultId) {
        FaultReport fault = faultMapper.selectById(faultId);
        if (fault == null) throw new BusinessException("故障记录不存在");
        fault.setStatus(4);
        faultMapper.updateById(fault);
    }

    @Override
    public void evaluateWorkOrder(Long orderId, Integer score, String remark) {
        FaultWorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("工单不存在");
        order.setEvaluateScore(score);
        order.setEvaluateRemark(remark);
        workOrderMapper.updateById(order);
    }

    @Override
    public Page<FaultWorkOrder> pageWorkOrders(int pageNum, int pageSize, Long assigneeId, Integer status) {
        Page<FaultWorkOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FaultWorkOrder> wrapper = new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(assigneeId != null, FaultWorkOrder::getAssigneeId, assigneeId)
                .eq(status != null, FaultWorkOrder::getStatus, status)
                .eq(FaultWorkOrder::getDeleted, 0)
                .orderByDesc(FaultWorkOrder::getCreateTime);
        return workOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public FaultWorkOrder getWorkOrderByFaultId(Long faultId) {
        return workOrderMapper.selectOne(new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getFaultId, faultId)
                .eq(FaultWorkOrder::getDeleted, 0)
                .orderByDesc(FaultWorkOrder::getCreateTime)
                .last("LIMIT 1"));
    }

    private void syncMaintenanceCost(FaultWorkOrder order, FaultReport fault) {
        BigDecimal cost = order.getCost();
        if (cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        MaintenanceCost maintenanceCost = costMapper.selectOne(new LambdaQueryWrapper<MaintenanceCost>()
                .eq(MaintenanceCost::getWorkOrderId, order.getId())
                .eq(MaintenanceCost::getDeleted, 0)
                .last("LIMIT 1"));
        boolean isNew = maintenanceCost == null;
        if (isNew) {
            maintenanceCost = new MaintenanceCost();
            maintenanceCost.setWorkOrderId(order.getId());
        }

        if (fault != null) {
            maintenanceCost.setDeviceId(fault.getDeviceId());
            maintenanceCost.setDeviceCode(fault.getDeviceCode());
            maintenanceCost.setDeviceName(fault.getDeviceName());
        }
        maintenanceCost.setCostType("repair");
        maintenanceCost.setCostAmount(cost);
        maintenanceCost.setCostDate(order.getActualEnd() != null ? order.getActualEnd().toLocalDate() : LocalDate.now());
        maintenanceCost.setDescription(order.getHandleDesc());
        maintenanceCost.setOperator(order.getAssigneeName());

        if (isNew) {
            costMapper.insert(maintenanceCost);
        } else {
            costMapper.updateById(maintenanceCost);
        }
    }

    private String resolveAssigneeName(WorkOrderImportDTO dto) {
        if (hasText(dto.getAssigneeName())) {
            return dto.getAssigneeName().trim();
        }
        if (dto.getAssigneeId() != null) {
            SysUser assignee = userMapper.selectById(dto.getAssigneeId());
            if (assignee != null) {
                return assignee.getRealName() != null ? assignee.getRealName() : assignee.getUsername();
            }
        }
        return "历史维护员";
    }

    private String resolveUniqueFaultNo(String input, LocalDateTime time) {
        String faultNo = hasText(input) ? input.trim() : buildNo("HFLT", time);
        if (faultMapper.selectCount(new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getFaultNo, faultNo)
                .eq(FaultReport::getDeleted, 0)) > 0) {
            throw new BusinessException("故障单号已存在");
        }
        return faultNo;
    }

    private String resolveUniqueOrderNo(String input, LocalDateTime time) {
        String orderNo = hasText(input) ? input.trim() : buildNo("HWO", time);
        if (workOrderMapper.selectCount(new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getOrderNo, orderNo)
                .eq(FaultWorkOrder::getDeleted, 0)) > 0) {
            throw new BusinessException("工单号已存在");
        }
        return orderNo;
    }

    private String buildNo(String prefix, LocalDateTime time) {
        return prefix + "-" + DateUtil.format(Date.from(time.atZone(java.time.ZoneId.systemDefault()).toInstant()), "yyyyMMdd")
                + "-" + System.currentTimeMillis() % 100000;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
