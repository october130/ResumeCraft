package com.example.myself_resume_analyzer.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
    /**
     * 计算字节数组的 MD5 值
     * @param data 文件内容（字节数组）
     * @return 32 位十六进制字符串（文件的"指纹"）
     */
    public static String calculateMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            //计算MD5哈希值
            byte[] md5Bytes = md.digest(data);

            // 把字节数组转成十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }
}


