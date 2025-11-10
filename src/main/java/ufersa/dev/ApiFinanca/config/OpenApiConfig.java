package ufersa.dev.ApiFinanca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Finança",
                version = "1.0",
                description = "Documentação da API de finanças pessoais"
        )
)
public class OpenApiConfig {
}



