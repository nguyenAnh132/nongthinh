package com.nongthinh.brand_service.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.nongthinh.brand_service.configuration.jwt.JwtBearerTokenResolver;
import com.nongthinh.brand_service.configuration.property.AuthCookieProperties;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({ApiKeyProperties.class, AuthCookieProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyProperties apiKeyProperties;
    private final JwtBearerTokenResolver jwtBearerTokenResolver;

    private final String[] PUBLIC_POST_ENDPOINTS = {
 
    };

    private final String[] PUBLIC_GET_ENDPOINTS = {
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable);

        http.addFilterBefore(
                new ApiKeyAuthenticationFilter(apiKeyProperties),
                UsernamePasswordAuthenticationFilter.class
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/camunda/**").permitAll()
                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                .anyRequest().authenticated());

        http.oauth2ResourceServer(
            oauth2 -> oauth2.jwt(
                jwtConfigurer -> jwtConfigurer
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
            .bearerTokenResolver(jwtBearerTokenResolver)
            .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new CustomAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }
}
