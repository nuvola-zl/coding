package com.zhanglin.homepage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 个人主页启动类
 *
 * @author 张林
 */
@SpringBootApplication
@MapperScan("com.zhanglin.homepage.mapper")
public class HomepageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomepageApplication.class, args);
    }

}
