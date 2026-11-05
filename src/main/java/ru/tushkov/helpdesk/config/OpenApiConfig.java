package ru.tushkov.helpdesk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helpdeskOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Сервис заявок технической поддержки")
                        .version("1.0.0")
                        .description("Регистрация заявок, смена статуса с историей и сводка за период. "
                                + "Интервал отчёта полуоткрытый: [from, to)."));
    }
}
