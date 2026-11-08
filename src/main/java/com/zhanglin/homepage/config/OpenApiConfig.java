package com.zhanglin.homepage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置，访问 http://localhost:8080/swagger-ui.html
 *
 * <p>页面上点右上角 Authorize 填入登录拿到的 token，就能直接调试需要权限的接口。</p>
 *
 * @author 张林
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI homepageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("个人主页 API")
                        .description("个人主页后端接口文档（练手项目）")
                        .version("v1.0.0")
                        .contact(new Contact().name("张林")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("登录接口返回的 token，不用自己加 Bearer 前缀")));
    }

}
