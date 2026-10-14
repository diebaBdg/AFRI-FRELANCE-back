package AfriFreelance.API.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();

        // Relative URL only: Swagger "Try it out" stays same-origin
        // (localhost vs 127.0.0.1, or port 8080 vs 8082, used to look like CORS).
        Server relativeServer = new Server();
        relativeServer.setUrl(contextPath);
        relativeServer.setDescription("Same origin (Swagger)");
        servers.add(relativeServer);

        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Entrez le token JWT obtenu via /auth/login");

        return new OpenAPI()
                .info(new Info()
                        .title("AfriFreelance API")
                        .version("1.0.0")
                        .description("API de la marketplace freelance AfriFreelance"))
                .servers(servers)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}