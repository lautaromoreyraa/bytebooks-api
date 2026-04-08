package com.bytebooks.api.security;

import com.bytebooks.api.security.jwt.JwtAccessDeniedHandler;
import com.bytebooks.api.security.jwt.JwtAuthenticationEntryPoint;
import com.bytebooks.api.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    JwtAuthenticationFilter jwtFilter,
                                                    JwtAuthenticationEntryPoint entryPoint,
                                                    JwtAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(entryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                // Publico
                .requestMatchers(HttpMethod.POST,   "/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,    "/libros", "/libros/**").permitAll()
                .requestMatchers(HttpMethod.GET,    "/categorias", "/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET,    "/usuarios/{id}").permitAll()
                .requestMatchers(HttpMethod.GET,    "/usuarios/*/favoritos").permitAll()
                // Moderador o Admin
                .requestMatchers(HttpMethod.PUT,    "/libros/**").hasAnyRole("MODERATOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/libros/**").hasAnyRole("MODERATOR", "ADMIN")
                // Solo Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/libros/*/resenas").authenticated()
                .requestMatchers(HttpMethod.POST,   "/libros/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMIN")
                // Cualquier usuario autenticado
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000))
                    .frameOptions(frame -> frame.deny())
                    .contentTypeOptions(cto -> {})
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
