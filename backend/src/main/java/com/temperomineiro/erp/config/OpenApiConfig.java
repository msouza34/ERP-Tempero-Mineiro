package com.temperomineiro.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Tempero Mineiro ERP API")
                        .version("1.0.0")
                        .description("API REST do ERP multi-restaurante para bares e restaurantes.")
                        .contact(new Contact().name("Tempero Mineiro ERP")))
                .tags(List.of(
                        new Tag().name("Autenticacao").description("Login e cadastro inicial do restaurante."),
                        new Tag().name("Mesas").description("Gestao operacional das mesas."),
                        new Tag().name("Categorias").description("Categorias do cardapio."),
                        new Tag().name("Produtos").description("Produtos e ficha tecnica."),
                        new Tag().name("Pedidos").description("Pedidos do salao e operacao."),
                        new Tag().name("Cozinha").description("Fila de preparo e stream da cozinha."),
                        new Tag().name("Caixa").description("Resumo da mesa, pagamentos e fechamento."),
                        new Tag().name("Estoque").description("Itens, ajustes e movimentacoes de estoque."),
                        new Tag().name("Relatorios").description("Indicadores e relatorios operacionais."),
                        new Tag().name("Usuarios").description("Usuarios e perfis do restaurante."),
                        new Tag().name("Publico").description("Endpoints publicos para menu, pedidos e QR Code."),
                        new Tag().name("Notificacoes").description("Canal de notificacoes em tempo real.")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .schemaRequirement(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
