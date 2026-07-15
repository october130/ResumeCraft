package com.example.myself_resume_analyzer.resume.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 上传文件到 OSS
     * @param file 文件
     * @param dir  目录前缀，如 "resumes"、"avatars"
     * @return 文件的完整 URL
     */
    String upload(MultipartFile file, String dir);

    /**
     * 删除 OSS 上的文件
     * @param fileUrl 文件的完整 URL
     */
    void delete(String fileUrl);

    /**
     * 从 OSS 下载文件
     * @param fileUrl 文件的完整 URL
     * @return 文件字节数组
     */
    byte[] download(String fileUrl);
}
