package com.campus.equipment.dto;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requireCoreDeviceFields() {
        DeviceDTO dto = new DeviceDTO();

        Set<ConstraintViolation<DeviceDTO>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "deviceCode".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "deviceName".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "categoryId".equals(v.getPropertyPath().toString())));
    }

    @Test
    void rejectNegativePriceAndInvalidPhone() {
        DeviceDTO dto = new DeviceDTO();
        dto.setDeviceCode("DEV-TEST-001");
        dto.setDeviceName("测试设备");
        dto.setCategoryId(1L);
        dto.setPurchasePrice(BigDecimal.valueOf(-1));
        dto.setResponsiblePhone("12345");

        Set<ConstraintViolation<DeviceDTO>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "purchasePrice".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "responsiblePhone".equals(v.getPropertyPath().toString())));
    }
}
