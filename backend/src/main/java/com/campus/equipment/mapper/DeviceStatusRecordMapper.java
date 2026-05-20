package com.campus.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.equipment.entity.DeviceStatusRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DeviceStatusRecordMapper extends BaseMapper<DeviceStatusRecord> {
    @Select("SELECT * FROM device_status_record WHERE device_id = #{deviceId} ORDER BY record_time DESC LIMIT #{limit}")
    List<DeviceStatusRecord> selectRecentByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") int limit);
}
