package com.example.stockgestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // (Les prochaines étapes ajouteront des Beans ici)

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Utilise l'algorithme BCrypt
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Hada l-bean #3: (L-Users li ṭleb l-brief)
        
        // ADMIN
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN") 
                .build();
        
        // WAREHOUSE_MANAGER
        UserDetails warehouseManager = User.builder()
                .username("manager")
                .password(passwordEncoder.encode("manager123"))
                .roles("WAREHOUSE_MANAGER")
                .build();
        
        // CLIENT
        UserDetails clientUser = User.builder()
                .username("client")
                .password(passwordEncoder.encode("client123"))
                .roles("CLIENT")
                .build();

        // Crée le gestionnaire d'utilisateurs avec les 3 comptes
        return new InMemoryUserDetailsManager(admin, warehouseManager, clientUser);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
           
            .csrf(csrf -> csrf.disable())

          
            .httpBasic(withDefaults())

            
            .authorizeHttpRequests(authz -> authz
                
                // Règle A: ADMIN (Produits, Entrepôts, Commandes Fournisseurs)
                .requestMatchers("/api/products/**").hasRole("ADMIN")
                .requestMatchers("/api/warehouses/**").hasRole("ADMIN")
                .requestMatchers("/api/purchase-orders/**").hasRole("ADMIN")
                // (Ajoutez ici /api/suppliers/** si vous l'avez)
                
                // Règle B: WAREHOUSE_MANAGER (Inventaire, Mouvements)
                .requestMatchers("/api/inventory/**").hasRole("WAREHOUSE_MANAGER")
                
                // Règle C: CLIENT (Commandes Client)
                .requestMatchers("/api/sales-orders/**").hasRole("CLIENT")
                
                // Règle D: Autoriser Swagger (pour les tests)
                .requestMatchers(
                    "/swagger-ui.html", 
                    "/swagger-ui/**", 
                    "/api-docs/**"
                ).permitAll()
                
                // Règle E (Finale): Tout le reste doit être authentifié
                .anyRequest().authenticated() 
            );

        return http.build();
    }

}
