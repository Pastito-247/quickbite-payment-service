package com.quickbite.payment_service.config;

import com.quickbite.payment_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health checks y endpoints públicos
                .requestMatchers("/actuator/health", "/actuator/info", "/api/health", "/api/ready").permitAll()
                
                // Endpoints de pagos - requieren autenticación
                .requestMatchers("/api/payments/process").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/payments/*/refund").hasAnyRole("ADMIN", "KITCHEN_STAFF")
                .requestMatchers("/api/payments/**").hasAnyRole("CUSTOMER", "ADMIN", "KITCHEN_STAFF")
                
                // Endpoints de billetera - solo usuarios autenticados
                .requestMatchers("/api/wallets/**").hasAnyRole("CUSTOMER", "ADMIN")
                
                // Endpoints de transacciones - acceso según rol
                .requestMatchers("/api/transactions/payment/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/transactions/**").hasAnyRole("ADMIN", "KITCHEN_STAFF")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
