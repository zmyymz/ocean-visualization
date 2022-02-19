package com.csu.oceanvisualization.servicebase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization
 * @date 2022/2/19 10:39
 */
@Configuration
// @EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket webApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("webApi")
                .apiInfo(webApiInfo())
                .select()
                .build();

    }

    private ApiInfo webApiInfo() {
        return new ApiInfoBuilder()
                .title("ocean-visualization API文档")
                .description("本文档描述了ocean-visualization接口定义")
                .version("1.0")
                .contact(new Contact("java", "http://ocean.com", "1123@qq.com"))
                .build();
    }

}
