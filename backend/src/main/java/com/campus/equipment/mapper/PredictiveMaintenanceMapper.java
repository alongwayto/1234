package com.campus.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.equipment.entity.PredictiveMaintenance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预测性维护 Mapper
 */
@Mapper
public interface PredictiveMaintenanceMapper extends BaseMapper<PredictiveMaintenance> {
}
