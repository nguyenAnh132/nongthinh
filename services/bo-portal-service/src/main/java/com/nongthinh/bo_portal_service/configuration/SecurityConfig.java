package com.nongthinh.bo_portal_service.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.nongthinh.bo_portal_service.configuration.jwt.JwtBearerTokenResolver;
import com.nongthinh.bo_portal_service.configuration.property.AuthCookieProperties;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({ApiKeyProperties.class, AuthCookieProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyProperties apiKeyProperties;
    private final JwtBearerTokenResolver jwtBearerTokenResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable);

        http.addFilterBefore(
                new ApiKeyAuthenticationFilter(apiKeyProperties),
                UsernamePasswordAuthenticationFilter.class
        );

        http.authorizeHttpRequests(auth -> auth
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
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CustomAuthoritiesConverter());
        return converter;
    }
}
