package com.example.springboot.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.example.springboot.common.Result;
import com.example.springboot.config.interceptor.AuthAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 文件上传下载Controller
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    // 文件上传存储路径
    @Value("${file.upload.path:files/}")
    private String uploadPath;

    // 服务器IP地址（兼容旧配置）
    @Value("${server.ip:localhost}")
    private String serverIp;

    // 服务器端口（兼容旧配置）
    @Value("${server.port:9090}")
    private String serverPort;

    // 文件访问域名（优先使用Nginx静态目录）
    @Value("${file.access.domain:}")
    private String fileAccessDomain;

    // 文件访问路径前缀
    @Value("${file.access.prefix:/uploads/}")
    private String fileAccessPrefix;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }

            // 获取文件后缀
            String suffix = FileUtil.getSuffix(originalFilename);
            
            // 验证文件类型（只允许图片）
            List<String> allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp");
            if (!allowedTypes.contains(suffix.toLowerCase())) {
                return Result.error("只支持上传图片文件（jpg、png、gif、webp）");
            }

            // 验证文件大小（不超过5MB）
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return Result.error("文件大小不能超过5MB");
            }

            // 生成唯一文件名
            String fileName = IdUtil.fastSimpleUUID() + "." + suffix;

            // 确保上传目录存在
            FileUtil.mkdir(uploadPath);

            // 保存文件到服务器
            String filePath = buildStoragePath(fileName);
            file.transferTo(FileUtil.file(filePath));

            // 生成访问URL
            String url = buildAccessUrl(fileName);

            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载/预览文件
     */
    @AuthAccess
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            // 构建文件路径
            String filePath = buildStoragePath(fileName);
            
            // 检查文件是否存在
            if (!FileUtil.exist(filePath)) {
                response.setStatus(404);
                return;
            }

            // 读取文件字节
            byte[] bytes = FileUtil.readBytes(filePath);

            // 设置响应头 - 使用inline方式在浏览器中直接显示图片
            response.setContentType("image/jpeg");
            response.setHeader("Content-Disposition", "inline; filename=" + 
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 写入响应流
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public Result uploadBatch(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("文件不能为空");
        }

        try {
            StringBuilder urls = new StringBuilder();
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // 获取原始文件名
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    continue;
                }

                // 获取文件后缀
                String suffix = FileUtil.getSuffix(originalFilename);
                
                // 验证文件类型
                List<String> allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp");
                if (!allowedTypes.contains(suffix.toLowerCase())) {
                    continue;
                }

                // 生成唯一文件名
                String fileName = IdUtil.fastSimpleUUID() + "." + suffix;

                // 确保上传目录存在
                FileUtil.mkdir(uploadPath);

                // 保存文件
                String filePath = buildStoragePath(fileName);
                file.transferTo(FileUtil.file(filePath));

                // 生成访问URL
                String url = buildAccessUrl(fileName);
                
                if (urls.length() > 0) {
                    urls.append(",");
                }
                urls.append(url);
            }

            return Result.success(urls.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    private String buildStoragePath(String fileName) {
        String normalizedUploadPath = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        return normalizedUploadPath + fileName;
    }

    private String buildAccessUrl(String fileName) {
        if (Objects.nonNull(fileAccessDomain) && !fileAccessDomain.isBlank()) {
            String domain = fileAccessDomain.endsWith("/")
                    ? fileAccessDomain.substring(0, fileAccessDomain.length() - 1)
                    : fileAccessDomain;
            String prefix = fileAccessPrefix.startsWith("/") ? fileAccessPrefix : "/" + fileAccessPrefix;
            String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
            return domain + normalizedPrefix + fileName;
        }
        return "http://" + serverIp + ":" + serverPort + "/api/file/download/" + fileName;
    }
}

