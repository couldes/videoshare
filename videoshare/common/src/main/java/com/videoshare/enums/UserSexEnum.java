// 路径: common/src/main/java/com/videoshare/enums/UserSexEnum.java
package com.videoshare.enums;

public enum UserSexEnum {
    MALE(0, "男"),
    FEMALE(1, "女"),
    SECRECY(2, "保密");  // 默认注册时不公开性别

    private final Integer type;
    private final String desc;

    UserSexEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() { return type; }
    public String getDesc() { return desc; }
}