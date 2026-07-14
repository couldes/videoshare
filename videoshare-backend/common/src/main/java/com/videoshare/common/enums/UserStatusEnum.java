// 路径: common/src/main/java/com/videoshare/enums/UserStatusEnum.java
package com.videoshare.common.enums;

public enum UserStatusEnum {
    // 每个枚举项携带一个 status 值
    DISABLE(0),   // 禁用状态
    ENABLE(1);    // 启用状态

    private final Integer status;

    // 构造函数：创建枚举时赋值
    UserStatusEnum(Integer status) {
        this.status = status;
    }

    // 对外提供获取值的方法
    public Integer getStatus() {
        return status;
    }
}