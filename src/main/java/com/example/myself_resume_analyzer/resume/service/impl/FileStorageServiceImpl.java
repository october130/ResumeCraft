package com.example.myself_resume_analyzer.resume.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.myself_resume_analyzer.common.config.OssConfig;
import com.example.myself_resume_analyzer.resume.service.FileStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Resource
    private OSS ossClient;

    @Resource
    private OssConfig ossConfig;

    private static final DateTimeFormatter DATE_PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public String upload(MultipartFile file, String dir) {
        log.info("开始上传文件: 原始文件名={}, 大小={}, dir={}", file.getOriginalFilename(), file.getSize(), dir);

        // 1. 生成日期目录，如 "2026/07/09"
        String datePath = LocalDate.now().format(DATE_PATH_FORMAT);

        // 2. 生成唯一文件名：UUID + 原始后缀
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 3. 拼接 objectKey：dir/日期/uuid.ext
        String objectKey = dir + "/" + datePath + "/" + uuid + extension;
        log.info("生成 objectKey: {}", objectKey);

        try {
            // 4. 设置文件元信息
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // 5. 上传到 OSS
            log.info("开始上传到 OSS, bucket={}, key={}", ossConfig.getBucketName(), objectKey);
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream, metadata);

            // 6. 拼接返回 URL
            String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectKey;
            log.info("文件上传成功: {} -> {}", originalFilename, url);
            return url;

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            String objectKey = getObjectKey(fileUrl);
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            // 删除失败不抛异常，不影响主业务流程
        }
    }

    @Override
    public byte[] download(String fileUrl) {//如果重新分析，就要从OSS重新下载简历

        try( OSSObject object = ossClient.getObject(ossConfig.getBucketName(), getObjectKey(fileUrl))) {
            byte[] resultTXT = object.getObjectContent().readAllBytes();
            return resultTXT;
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败" + e.getMessage(), e);
        }

    }

    @NotNull
    private String getObjectKey(String fileUrl) {
        // 从 URL 中提取 objectKey
        // URL 格式: https://bucket.endpoint/objectKey
        String prefix = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/";
        String objectKey = fileUrl.replace(prefix, "");
        return objectKey;
    }
}
