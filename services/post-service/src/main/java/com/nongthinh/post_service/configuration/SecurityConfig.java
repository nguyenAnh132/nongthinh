package com.nongthinh.post_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtBearerTokenResolver bearerTokenResolver,
                                           JwtAuthenticationEntryPoint authenticationEntryPoint)
            throws Exception {
        http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/post-types", "/post-topics",
                                "/post-topics/trending").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/feed", "/{id}",
                                "/{postId}/media", "/{postId}/media/{mediaId}",
                                "/{postId}/comments", "/{postId}/reactions",
                                "/{postId}/shares", "/{postId}/engagement").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .bearerTokenResolver(bearerTokenResolver)
                        .authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CustomAuthoritiesConverter());
        return converter;
    }
}
