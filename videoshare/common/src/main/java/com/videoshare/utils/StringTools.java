// 路径: common/src/main/java/com/videoshare/utils/StringTools.java
package com.videoshare.utils;

import java.security.MessageDigest;
import java.util.Random;

public class StringTools {

    /**
     * 生成指定长度的随机数字字符串，用作用户ID
     * 例如：getRandomNumber(10) → "3748291056"
     */
    public static String getRandomNumber(Integer length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));  // 每次追加 0-9 中的一个数字
        }
        return sb.toString();
    }

    /**
     * MD5 加密密码
     * 例如：encodeByMd5("123456") → "e10adc3949ba59abbe56e057f20f883e"
     * 特点：不可逆，数据库只存密文，即使泄露也无法还原原密码
     */
    public static String encodeByMd5(String originString) {
        try {
            // 获取 MD5 加密算法实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 把字符串转成字节数组后加密
            byte[] result = md.digest(originString.getBytes());
            // 把字节数组转成16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                // 将每个字节格式化为2位16进制数（不足补0）
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // 实际中建议用自定义异常，这里简化处理
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}