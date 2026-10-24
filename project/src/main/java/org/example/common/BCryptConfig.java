package org.example.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码编码器配置。
 *
 * BCryptPasswordEncoder 是 Spring Security 提供的安全密码哈希工具：
 *   - 每次 encode() 自动生成随机盐，相同明文每次哈希结果不同
 *   - matches() 做常量时间比对，防时序攻击
 *   - strength=10（默认）意味着 2^10 轮运算，在安全性和性能之间取平衡
 *
 * 注册成单例 Bean，Service 层构造函数注入即可使用。
 */
@Configuration
public class BCryptConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}