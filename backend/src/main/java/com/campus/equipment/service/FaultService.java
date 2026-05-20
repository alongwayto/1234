package com.campus.equipment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.dto.FaultReportDTO;
import com.campus.equipment.dto.WorkOrderAssignDTO;
import com.campus.equipment.dto.WorkOrderHandleDTO;
import com.campus.equipment.dto.WorkOrderImportDTO;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.FaultWorkOrder;

public interface FaultService {
    Page<FaultReport> pageFaults(int pageNum, int pageSize, Long deviceId, Integer status, Integer faultLevel);
    FaultReport getFaultById(Long id);
    FaultReport createFault(FaultReportDTO dto, String reporterName, Long reporterId);
    FaultWorkOrder assignWorkOrder(WorkOrderAssignDTO dto, String assignerName, Long assignerId);
    FaultWorkOrder importHistoricalWorkOrder(WorkOrderImportDTO dto, String operatorName);
    void startHandle(Long orderId);
    void completeHandle(WorkOrderHandleDTO dto);
    void archiveFault(Long faultId);
    void evaluateWorkOrder(Long orderId, Integer score, String remark);
    Page<FaultWorkOrder> pageWorkOrders(int pageNum, int pageSize, Long assigneeId, Integer status);
    FaultWorkOrder getWorkOrderByFaultId(Long faultId);
}
