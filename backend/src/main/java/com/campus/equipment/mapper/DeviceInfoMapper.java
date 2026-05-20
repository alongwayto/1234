package com.campus.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.vo.DeviceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceInfoMapper extends BaseMapper<DeviceInfo> {
    IPage<DeviceVO> selectDevicePage(Page<DeviceVO> page,
                                     @Param("deviceCode") String deviceCode,
                                     @Param("deviceName") String deviceName,
                                     @Param("categoryId") Long categoryId,
                                     @Param("locationId") Long locationId,
                                     @Param("status") Integer status,
                                     @Param("onlineStatus") Integer onlineStatus);
}
