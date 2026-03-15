// 路径: web/src/main/java/com/videoshare/web/service/impl/UserInfoServiceImpl.java
package com.videoshare.web.service.impl;

import com.videoshare.constants.Constants;
import com.videoshare.entity.UserInfo;
import com.videoshare.enums.UserSexEnum;
import com.videoshare.enums.UserStatusEnum;
import com.videoshare.exception.BusinessException;
import com.videoshare.utils.StringTools;
import com.videoshare.web.component.RedisComponent;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.service.UserInfoService;
import com.videoshare.web.vo.ResponseVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private RedisComponent redisComponent; // Service 层直接持有 Redis 组件

    // ============================================================
    //  登录
    // ============================================================

    /**
     * 登录方法（所有验证逻辑全在这里）
     *
     * @param email         邮箱
     * @param password      密码（前端传来的明文）
     * @param checkCode     用户填写的验证码
     * @param checkCodeKey  验证码凭证Key（从 /checkCode 接口拿到的）
     * @return ResponseVO   登录成功后返回的用户信息（含Token）
     */
    @Override
    public ResponseVO login(String email, String password,
                            String checkCode, String checkCodeKey) {
        try {
            // =====================
            // 第一关：验证码校验
            // =====================

            // ① 拿着 Key 去 Redis 取出正确答案
            String savedCode = redisComponent.getCheckCode(checkCodeKey);

            // ② savedCode == null 说明验证码已过期（Redis 自动清除了）
            //    !equalsIgnoreCase 说明用户填错了
            if (savedCode == null || !savedCode.equalsIgnoreCase(checkCode)) {
                // 抛出业务异常，Service 内部统一用异常表达错误
                throw new BusinessException("验证码错误或已过期");
            }

            // =====================
            // 第二关：账号是否存在
            // =====================

            // ③ 根据邮箱查用户
            UserInfo userInfo = userInfoMapper.selectByEmail(email);

            // ④ 查不到 → 账号不存在
            if (userInfo == null) {
                throw new BusinessException("账号或密码错误"); // 不说"账号不存在"，防止用户枚举
            }

            // =====================
            // 第三关：密码校验
            // =====================

            // ⑤ 把用户输入的明文密码也做 MD5 加密，再和数据库里的密文比较
            //    数据库存的是 MD5("123456") = "e10adc3..."
            //    用户输入 "123456" → MD5("123456") = "e10adc3..." → 匹配 ✅
            String encodedPassword = StringTools.encodeByMd5(password);
            if (!encodedPassword.equals(userInfo.getPassword())) {
                throw new BusinessException("账号或密码错误");
            }

            // =====================
            // 第四关：账号状态
            // =====================

            // ⑥ 账号被禁用
            if (!UserStatusEnum.ENABLE.getStatus().equals(userInfo.getStatus())) {
                throw new BusinessException("账号已被禁用，请联系管理员");
            }

            // =====================
            // 全部通过：生成 Token，构建返回对象
            // =====================

            // ⑦ 生成 Token（登录凭证）
            // Token 是一个随机字符串，后续请求带上它来证明"我已登录"
            // 格式：userId_随机数，保证唯一性
            String token = userInfo.getUserId() + StringTools.getRandomNumber(10);

            // ⑧ 把 Token 存入 Redis，有效期7天
            //    Key = "TOKEN_" + token，Value = userId
            //    下次请求带 token 来，后端用它查 Redis 就知道是谁
            redisComponent.saveUserToken(token, userInfo.getUserId());

            // ⑨ 构建 VO（只包含前端需要的字段，不含 password 等敏感信息）
            ResponseVO ResponseVO = new ResponseVO();
            ResponseVO.setUserId(userInfo.getUserId());
            ResponseVO.setNickName(userInfo.getNickName());
            ResponseVO.setEmail(userInfo.getEmail());
            ResponseVO.setSex(userInfo.getSex());
            ResponseVO.setTheme(userInfo.getTheme());
            ResponseVO.setToken(token); // Token 告诉前端，后续请求带上它

            return ResponseVO;

        } finally {
            // ⑩ finally：无论登录成功还是失败，验证码都只能用一次
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    // ============================================================
    //  注册（原有，无需改动）
    // ============================================================
    @Override
    public void register(String email, String nickName, String registerPassword,
                         String checkCode, String checkCodeKey) {
        try {
            // ① 验证码校验（和 login 完全一致的逻辑）
            String savedCode = redisComponent.getCheckCode(checkCodeKey);
            if (savedCode == null || !savedCode.equalsIgnoreCase(checkCode)) {
                throw new BusinessException("验证码错误或已过期");
            }

            // ② 邮箱重复校验
            UserInfo userInfo = userInfoMapper.selectByEmail(email);
            if (null != userInfo) {
                throw new BusinessException("邮箱账号已经存在");
            }

            // ③ 昵称重复校验
            UserInfo nickNameUser = userInfoMapper.selectByNickName(nickName);
            if (null != nickNameUser) {
                throw new BusinessException("昵称已经存在");
            }

            // ④ 构建用户对象并写库（不变）
            userInfo = new UserInfo();
            String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
            userInfo.setUserId(userId);
            userInfo.setNickName(nickName);
            userInfo.setEmail(email);
            userInfo.setPassword(StringTools.encodeByMd5(registerPassword));
            userInfo.setJoinTime(new Date());
            userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
            userInfo.setSex(UserSexEnum.SECRECY.getType());
            userInfo.setTheme(Constants.ONE);
            userInfo.setTotalCoinCount(10);
            userInfoMapper.insert(userInfo);

        } finally {
            // ⑤ 无论成败，销毁验证码（和 login 保持一致）
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @Override
    public Integer deleteUserInfoByNickName(String nickName) {
        return userInfoMapper.deleteByNickName(nickName);
    }
}