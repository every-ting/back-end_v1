package com.ting.ting.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Ting app API",
                version = "0.0",
                description = "생성 AI를 이용한 이상형 매칭 과팅 & 소개팅 어플리케이션"
        )
)
public class SwaggerConfig {
}
