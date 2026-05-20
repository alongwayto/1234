package com.campus.equipment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.campus.equipment.mapper")
@EnableScheduling
public class EquipmentManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EquipmentManagementApplication.class, args);
    }
}
