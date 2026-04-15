
package com.videoshare.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


//读取 application.yml 中 admin.* 配置项
//@ConfigurationProperties(prefix = "admin")
//会自动把 yml 里 admin.account / admin.password 注入到字段里
@Component
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private String  account;   // 管理员账号（对应 admin.account）
    private String  password;  // 管理员密码（对应 admin.password）
    private Integer tokenTtl;  // Token 有效期（天）（对应 admin.token-ttl，短横线自动转驼峰）

    public String getAccount()  {//写 getter 是为了获取配置值,外部要用这些配置时调用
        return account; }

    public void setAccount(String account) {//为了让 Spring Boot 能注入配置值
        this.account = account; }

    public String getPassword() {
        return password; }

    public void setPassword(String password) {
        this.password = password; }

    public Integer getTokenTtl() {
        return tokenTtl; }

    public void setTokenTtl(Integer tokenTtl) {
        this.tokenTtl = tokenTtl; }
}
