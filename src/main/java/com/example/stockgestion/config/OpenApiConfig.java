package com.example.stockgestion.config;

import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("StockGestion API - Gestion Logistique")
                        .version("1.0.0")
                        .description("""
                                API REST modulaire pour la gestion logistique complète :
                                - Gestion des produits (SKU)
                                - Stocks multi-entrepôts
                                - Commandes clients avec réservation
                                - Approvisionnements fournisseurs
                                - Expéditions et suivi des livraisons
                                - Traçabilité complète des opérations
                                """)
                        .contact(new Contact()
                                .name("Équipe StockGestion")
                                .email("contact@stockgestion.com")
                                .url("https://github.com/Mohamed-Hmidouch/Stockflow"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Serveur de développement"),
                        new Server()
                                .url("https://api.stockgestion.com")
                                .description("Serveur de production")
                ));
    }
}
