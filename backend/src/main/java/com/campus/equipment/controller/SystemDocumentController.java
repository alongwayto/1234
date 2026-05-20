package com.campus.equipment.controller;

import com.campus.equipment.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Tag(name = "系统文档")
@RestController
@RequestMapping("/system/docs")
public class SystemDocumentController {

    private static final Map<String, String> DOC_FILES = Map.of(
            "deployment", "DEPLOYMENT.md",
            "user-guide", "USER_GUIDE.md"
    );

    @Operation(summary = "打开系统文档")
    @GetMapping(value = "/{type}", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @PreAuthorize("hasAuthority('system:user')")
    public String getDocument(@PathVariable String type) throws IOException {
        String fileName = DOC_FILES.get(type);
        if (fileName == null) {
            throw new BusinessException("文档类型不存在");
        }
        Path file = resolveDocument(fileName);
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    private Path resolveDocument(String fileName) {
        Path dir = Paths.get("").toAbsolutePath().normalize();
        for (int i = 0; i < 4 && dir != null; i++) {
            Path candidate = dir.resolve(fileName).normalize();
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new BusinessException("文档文件不存在: " + fileName);
    }
}
