// 路径: web/src/main/java/com/videoshare/web/service/UserInfoService.java
package com.videoshare.web.service;

import com.videoshare.web.vo.ResponseVO;

public interface UserInfoService {

    // ✅ 补全：加入 checkCode 和 checkCodeKey 两个参数
    void register(String email, String nickName, String registerPassword,
                  String checkCode, String checkCodeKey);

    ResponseVO login(String email, String password,
                     String checkCode, String checkCodeKey);

    Integer deleteUserInfoByNickName(String nickName);
}