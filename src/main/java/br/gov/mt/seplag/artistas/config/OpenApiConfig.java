package br.gov.mt.seplag.artistas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Artistas e Álbuns")
                        .version("1.0.0")
                        .description("API REST para gerenciamento de artistas e álbuns musicais. " +
                                "Projeto prático para vaga de Desenvolvedor Back End - SEPLAG/MT")
                        .contact(new Contact()
                                .name("SEPLAG - Mato Grosso")
                                .url("https://www.seplag.mt.gov.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token de autenticação. " +
                                                "Obtenha o token em /api/v1/auth/login e use no formato: Bearer <token>")));
    }
}
