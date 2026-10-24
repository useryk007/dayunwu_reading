package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类。
 *
 * @SpringBootApplication 是组合注解，包含了：
 *   @Configuration       — 将本类声明为 Spring 配置类（内部使用 @Bean 方法注册组件）
 *   @EnableAutoConfiguration — 让 Spring Boot 根据 classpath 中的依赖自动装配（如 Tomcat、DataSource 等）
 *   @ComponentScan       — 扫描当前包及子包下的 @Component/@Service/@Controller 等组件
 *
 * @MapperScan("org.example.Mapper") 告诉 MyBatis 去哪里找 Mapper 接口。
 * 这样每个 Mapper 接口上就不用单独加 @Mapper 注解了，所有在该包下的接口自动注册为 Spring Bean。
 */
@SpringBootApplication
@MapperScan("org.example.Mapper")
public class ReadingApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用，内部会创建 Spring 容器、初始化嵌入式 Tomcat、加载所有配置与 Bean
        SpringApplication.run(ReadingApplication.class, args);
    }
}