
package com.videoshare.web.service.impl;

import com.videoshare.common.constants.Constants;
import com.videoshare.common.entity.UserInfo;
import com.videoshare.common.enums.UserSexEnum;
import com.videoshare.common.enums.UserStatusEnum;
import com.videoshare.common.exception.BusinessException;
import com.videoshare.common.utils.StringTools;
import com.videoshare.web.component.RedisComponent;
import com.videoshare.web.mapper.UserInfoMapper;
import com.videoshare.web.service.UserInfoService;
import com.videoshare.common.vo.ResponseVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private RedisComponent redisComponent;


    @Override
    public ResponseVO login(String email,
                            String password,
                            String checkCode,
                            String checkCodeKey) {
        try {

            // 拿着 Key 去 Redis 取出正确答案
            String savedCode = redisComponent.getCheckCode(checkCodeKey);

            // savedCode == null 说明验证码已过期（Redis 自动清除）。!equalsIgnoreCase 说明用户填错
            if (savedCode == null || !savedCode.equalsIgnoreCase(checkCode)) {
                throw new BusinessException("验证码错误或已过期");
            }



            // 账号是否存在，根据邮箱查用户
            UserInfo userInfo = userInfoMapper.selectByEmail(email);

            // 查不到，账号不存在
            if (userInfo == null) {
                throw new BusinessException("账号或密码错误"); // 不说"账号不存在"，防止用户枚举
            }



            // 密码校验
            //把用户输入的明文密码也做 MD5 加密，再和数据库里的密文比较
            String encodedPassword = StringTools.encodeByMd5(password);
            if (!encodedPassword.equals(userInfo.getPassword())) {
                throw new BusinessException("账号或密码错误");
            }



            // 查询账号状态
            //账号被禁用
            if (!UserStatusEnum.ENABLE.getStatus().equals(userInfo.getStatus())) {
                throw new BusinessException("账号已被禁用，请联系管理员");
            }



            // 全部通过：生成 Token，构建返回对象
            //  生成 Token，格式：userId_随机数，保证唯一性
            String token = userInfo.getUserId() + StringTools.getRandomNumber(10);

            //把 Token 存入 Redis，有效期7天
            //Key = "TOKEN_" + token，Value = userId
            redisComponent.saveUserToken(token, userInfo.getUserId());

            //构建 VO（只包含前端需要的字段，不含 password 等敏感信息）
            ResponseVO ResponseVO = new ResponseVO();
            ResponseVO.setUserId(userInfo.getUserId());
            ResponseVO.setNickName(userInfo.getNickName());
            ResponseVO.setEmail(userInfo.getEmail());
            ResponseVO.setTheme(userInfo.getTheme());
            ResponseVO.setToken(token); // Token 告诉前端，后续请求带上它

            return ResponseVO;

        } finally {
            //finally：无论登录成功还是失败，验证码都只能用一次
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }


    @Override
    public void register(String email, String nickName, String registerPassword, String checkCode, String checkCodeKey) {
        try {
            //验证码校验（和 login 完全一致的逻辑）
            String savedCode = redisComponent.getCheckCode(checkCodeKey);
            if (savedCode == null || !savedCode.equalsIgnoreCase(checkCode)) {
                throw new BusinessException("验证码错误或已过期");
            }

            //邮箱重复校验
            UserInfo userInfo = userInfoMapper.selectByEmail(email);
            if (null != userInfo) {
                throw new BusinessException("邮箱账号已经存在");
            }

            //昵称重复校验
            UserInfo nickNameUser = userInfoMapper.selectByNickName(nickName);
            if (null != nickNameUser) {
                throw new BusinessException("昵称已经存在");
            }

            //构建用户对象并写库（不变）
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
            userInfoMapper.insert(userInfo);

        } finally {
            //无论成败，销毁验证码（和 login 保持一致）
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @Override
    public Integer deleteUserInfoByNickName(String nickName) {
        return userInfoMapper.deleteByNickName(nickName);
    }
}