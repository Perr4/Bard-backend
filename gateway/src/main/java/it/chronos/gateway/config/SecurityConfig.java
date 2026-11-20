package it.chronos.gateway.config;

import it.chronos.gateway.security.BearerTokenServerAuthenticationConverter;
import it.chronos.gateway.security.JwtAuthenticationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationManager authenticationManager,
            BearerTokenServerAuthenticationConverter converter
    ) {

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);
        jwtFilter.setServerAuthenticationConverter(converter);
        jwtFilter.setAuthenticationFailureHandler(((webFilterExchange, exception) -> {
            System.out.println("Autenticazione fallita: " + (exception != null ? exception.getMessage() : "null"));

            ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String jsonResponse = String.format(
                    "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                    exception != null ? exception.getMessage() : "Token scaduto o non valido"
            );



            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        }));
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Disabilita HTTP Basic
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable) // Disabilita Form Login
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/auth/login").permitAll()
                        .pathMatchers("/auth/login").permitAll()
                        .pathMatchers("/login").permitAll()
                        .pathMatchers("/api/auth/register").permitAll()
                        // /api/auth/token e' un endpoint di introspezione: deve poter
                        // essere chiamato anche con un token non valido, quindi la
                        // verifica la fa auth-service (vedi AuthController.checkToken)
                        .pathMatchers("/api/auth/token").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/actuator/info").permitAll()
                                        .pathMatchers(HttpMethod.POST, "/api/presenza/**").hasRole("ADMIN")
                                        .pathMatchers(HttpMethod.PUT, "/api/presenza/**").hasRole("ADMIN")
                                        .pathMatchers(HttpMethod.DELETE, "/api/presenza/**").hasRole("ADMIN")
                                        .pathMatchers(HttpMethod.POST, "/api/uscite/**").hasRole("ADMIN")
                                        .pathMatchers(HttpMethod.DELETE, "/api/uscite/**").hasRole("ADMIN")
                                                .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(ex->ex
                                .accessDeniedHandler((exchange, ex1)->{
                                    ServerHttpResponse response = exchange.getResponse();
                                    response.setStatusCode(HttpStatus.FORBIDDEN);
                                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                                    String jsonResponse = String.format(
                                            "{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"%s\"}",
                                            ex1 != null ? ex1.getMessage() : "Accesso negato"
                                    );

                                    byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                                    DataBuffer buffer = response.bufferFactory().wrap(bytes);

                                    return response.writeWith(Mono.just(buffer));
                                })
                                .authenticationEntryPoint((exchange, ex1) ->{
                                    System.out.println("SSSS");
                                    ServerHttpResponse response = exchange.getResponse();
                                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                                    String jsonResponse = String.format(
                                            "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                                            ex1 != null ? ex1.getMessage() : "Token scaduto o non valido"
                                    );

                                    byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                                    DataBuffer buffer = response.bufferFactory().wrap(bytes);

                                    return response.writeWith(Mono.just(buffer));
                                        })

                                        )

                .build();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${CHRONOS_FRONTEND_ORIGIN:http://localhost:4200}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();

        // Lista separata da virgole: http://localhost:4200,http://192.168.1.22:4200
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-CSRF-TOKEN"
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}