package com.campus.equipment.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.equipment.dto.DeviceDTO;
import com.campus.equipment.entity.DeviceCategory;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.DeviceLocation;
import com.campus.equipment.excel.DeviceExcelModel;
import com.campus.equipment.exception.BusinessException;
import com.campus.equipment.mapper.DeviceCategoryMapper;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.DeviceLocationMapper;
import com.campus.equipment.service.DeviceService;
import com.campus.equipment.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceInfoMapper deviceMapper;
    private final DeviceCategoryMapper categoryMapper;
    private final DeviceLocationMapper locationMapper;

    @Override
    public IPage<DeviceVO> pageDevices(int pageNum, int pageSize, String deviceCode, String deviceName,
                                       Long categoryId, Long locationId, Integer status, Integer onlineStatus) {
        Page<DeviceVO> page = new Page<>(pageNum, pageSize);
        return deviceMapper.selectDevicePage(page, deviceCode, deviceName, categoryId, locationId, status, onlineStatus);
    }

    @Override
    public DeviceVO getDeviceById(Long id) {
        // 直接查单条
        DeviceInfo device = deviceMapper.selectById(id);
        if (device == null) throw new BusinessException("设备不存在");
        DeviceVO vo = new DeviceVO();
        BeanUtils.copyProperties(device, vo);
        // 填充分类名
        if (device.getCategoryId() != null) {
            DeviceCategory cat = categoryMapper.selectById(device.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        // 填充位置名
        if (device.getLocationId() != null) {
            DeviceLocation loc = locationMapper.selectById(device.getLocationId());
            if (loc != null) vo.setLocationName(loc.getFullAddress());
        }
        return vo;
    }

    @Override
    public void createDevice(DeviceDTO dto) {
        if (deviceMapper.selectOne(new LambdaQueryWrapper<DeviceInfo>()
                .eq(DeviceInfo::getDeviceCode, dto.getDeviceCode())) != null) {
            throw new BusinessException("设备编号已存在");
        }
        DeviceInfo device = new DeviceInfo();
        BeanUtils.copyProperties(dto, device);
        if (device.getStatus() == null) device.setStatus(1);
        device.setOnlineStatus(0);
        deviceMapper.insert(device);
    }

    @Override
    public void updateDevice(DeviceDTO dto) {
        DeviceInfo existing = deviceMapper.selectById(dto.getId());
        if (existing == null) throw new BusinessException("设备不存在");
        BeanUtils.copyProperties(dto, existing);
        deviceMapper.updateById(existing);
    }

    @Override
    public void deleteDevice(Long id) {
        if (deviceMapper.selectById(id) == null) throw new BusinessException("设备不存在");
        deviceMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        ids.forEach(this::deleteDevice);
    }

    @Override
    @Transactional
    public void importDevices(MultipartFile file) throws IOException {
        List<DeviceExcelModel> list = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), DeviceExcelModel.class, new AnalysisEventListener<DeviceExcelModel>() {
            @Override
            public void invoke(DeviceExcelModel data, AnalysisContext context) {
                list.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();

        // 构建分类 code->id 映射
        Map<String, Long> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(DeviceCategory::getCode, DeviceCategory::getId, (a, b) -> a));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (DeviceExcelModel row : list) {
            if (row.getDeviceCode() == null || row.getDeviceName() == null) continue;
            DeviceInfo device = new DeviceInfo();
            device.setDeviceCode(row.getDeviceCode());
            device.setDeviceName(row.getDeviceName());
            device.setCategoryId(catMap.get(row.getCategoryCode()));
            device.setBrand(row.getBrand());
            device.setModel(row.getModel());
            device.setSerialNumber(row.getSerialNumber());
            if (row.getPurchaseDate() != null) {
                try { device.setPurchaseDate(LocalDate.parse(row.getPurchaseDate(), fmt)); } catch (Exception ignored) {}
            }
            device.setPurchasePrice(row.getPurchasePrice());
            if (row.getWarrantyDate() != null) {
                try { device.setWarrantyDate(LocalDate.parse(row.getWarrantyDate(), fmt)); } catch (Exception ignored) {}
            }
            device.setResponsiblePerson(row.getResponsiblePerson());
            device.setResponsiblePhone(row.getResponsiblePhone());
            device.setTags(row.getTags());
            device.setDescription(row.getDescription());
            device.setStatus(1);
            device.setOnlineStatus(0);
            // 处理位置
            if (row.getBuilding() != null) {
                DeviceLocation loc = locationMapper.selectOne(
                        new LambdaQueryWrapper<DeviceLocation>()
                                .eq(DeviceLocation::getBuilding, row.getBuilding())
                                .eq(row.getRoom() != null, DeviceLocation::getRoom, row.getRoom()));
                if (loc == null) {
                    loc = new DeviceLocation();
                    loc.setBuilding(row.getBuilding());
                    loc.setFloor(row.getFloor());
                    loc.setRoom(row.getRoom());
                    loc.setFullAddress(row.getBuilding() + " " + (row.getFloor() != null ? row.getFloor() : "") + " " + (row.getRoom() != null ? row.getRoom() : ""));
                    locationMapper.insert(loc);
                }
                device.setLocationId(loc.getId());
            }
            // 跳过已存在的设备编号
            if (deviceMapper.selectOne(new LambdaQueryWrapper<DeviceInfo>().eq(DeviceInfo::getDeviceCode, device.getDeviceCode())) == null) {
                deviceMapper.insert(device);
            }
        }
    }

    @Override
    public void exportDevices(HttpServletResponse response, String deviceCode, String deviceName,
                              Long categoryId, Integer status) throws IOException {
        IPage<DeviceVO> page = pageDevices(1, 10000, deviceCode, deviceName, categoryId, null, status, null);
        List<DeviceExcelModel> exportList = page.getRecords().stream().map(vo -> {
            DeviceExcelModel m = new DeviceExcelModel();
            m.setDeviceCode(vo.getDeviceCode());
            m.setDeviceName(vo.getDeviceName());
            m.setBrand(vo.getBrand());
            m.setModel(vo.getModel());
            m.setSerialNumber(vo.getSerialNumber());
            if (vo.getPurchaseDate() != null) m.setPurchaseDate(vo.getPurchaseDate().toString());
            m.setPurchasePrice(vo.getPurchasePrice());
            if (vo.getWarrantyDate() != null) m.setWarrantyDate(vo.getWarrantyDate().toString());
            m.setResponsiblePerson(vo.getResponsiblePerson());
            m.setResponsiblePhone(vo.getResponsiblePhone());
            m.setTags(vo.getTags());
            m.setDescription(vo.getDescription());
            return m;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("设备列表", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), DeviceExcelModel.class).sheet("设备列表").doWrite(exportList);
    }

    @Override
    public void exportImportTemplate(HttpServletResponse response) throws IOException {
        DeviceExcelModel sample = new DeviceExcelModel();
        sample.setDeviceCode("DEV-2026-001");
        sample.setDeviceName("示例设备");
        sample.setCategoryCode("COMPUTER");
        sample.setBuilding("教学楼A");
        sample.setFloor("1F");
        sample.setRoom("101机房");
        sample.setBrand("联想");
        sample.setModel("ThinkCentre");
        sample.setSerialNumber("SN20260001");
        sample.setPurchaseDate("2026-05-04");
        sample.setWarrantyDate("2029-05-04");
        sample.setResponsiblePerson("张维修");
        sample.setResponsiblePhone("13800000002");
        sample.setTags("教学,计算机");
        sample.setDescription("导入时设备编号和设备名称必填，分类编码需与系统分类编码一致。");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("设备导入模板", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), DeviceExcelModel.class)
                .sheet("导入模板")
                .doWrite(List.of(sample));
    }

    @Override
    public void updateOnlineStatus(Long id, Integer onlineStatus) {
        DeviceInfo device = deviceMapper.selectById(id);
        if (device != null) {
            device.setOnlineStatus(onlineStatus);
            deviceMapper.updateById(device);
        }
    }

    // ===== 分类管理 =====
    @Override
    public List<DeviceCategory> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<DeviceCategory>()
                .eq(DeviceCategory::getDeleted, 0).orderByAsc(DeviceCategory::getSortOrder));
    }

    @Override
    public void createCategory(DeviceCategory category) {
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(DeviceCategory category) {
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    // ===== 位置管理 =====
    @Override
    public List<DeviceLocation> listLocations() {
        return locationMapper.selectList(new LambdaQueryWrapper<DeviceLocation>()
                .eq(DeviceLocation::getDeleted, 0).orderByAsc(DeviceLocation::getBuilding));
    }

    @Override
    public void createLocation(DeviceLocation location) {
        if (location.getFullAddress() == null) {
            location.setFullAddress(location.getBuilding() + " " +
                    (location.getFloor() != null ? location.getFloor() : "") + " " +
                    (location.getRoom() != null ? location.getRoom() : ""));
        }
        locationMapper.insert(location);
    }

    @Override
    public void updateLocation(DeviceLocation location) {
        locationMapper.updateById(location);
    }

    @Override
    public void deleteLocation(Long id) {
        locationMapper.deleteById(id);
    }
}
