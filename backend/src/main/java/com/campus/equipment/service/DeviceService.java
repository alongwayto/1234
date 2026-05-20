package com.campus.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.equipment.dto.DeviceDTO;
import com.campus.equipment.entity.DeviceCategory;
import com.campus.equipment.entity.DeviceLocation;
import com.campus.equipment.vo.DeviceVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface DeviceService {
    IPage<DeviceVO> pageDevices(int pageNum, int pageSize, String deviceCode, String deviceName,
                                Long categoryId, Long locationId, Integer status, Integer onlineStatus);
    DeviceVO getDeviceById(Long id);
    void createDevice(DeviceDTO dto);
    void updateDevice(DeviceDTO dto);
    void deleteDevice(Long id);
    void batchDelete(List<Long> ids);
    void importDevices(MultipartFile file) throws IOException;
    void exportImportTemplate(HttpServletResponse response) throws IOException;
    void exportDevices(HttpServletResponse response, String deviceCode, String deviceName,
                       Long categoryId, Integer status) throws IOException;
    void updateOnlineStatus(Long id, Integer onlineStatus);

    // 分类管理
    List<DeviceCategory> listCategories();
    void createCategory(DeviceCategory category);
    void updateCategory(DeviceCategory category);
    void deleteCategory(Long id);

    // 位置管理
    List<DeviceLocation> listLocations();
    void createLocation(DeviceLocation location);
    void updateLocation(DeviceLocation location);
    void deleteLocation(Long id);
}
