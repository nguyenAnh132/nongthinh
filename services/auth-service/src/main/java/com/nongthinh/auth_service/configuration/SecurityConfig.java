package com.nongthinh.auth_service.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.nongthinh.auth_service.configuration.apikeyconfig.ApiKeyAuthenticationFilter;
import com.nongthinh.auth_service.configuration.apikeyconfig.ApiKeyProperties;
import com.nongthinh.auth_service.configuration.jwt.CustomAuthoritiesConverter;
import com.nongthinh.auth_service.configuration.jwt.JwtAuthenticationEntryPoint;
import com.nongthinh.auth_service.configuration.jwt.JwtBearerTokenResolver;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(ApiKeyProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_POST_ENDPOINTS = {
        "/farmers",
        "/brands",
        "/otp/verify",
        "/otp/resend",
        "/refresh"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
        "/login",
        "/logout",
        "/otp/config",
        "/test/trace"
    };

    private final JwtBearerTokenResolver jwtBearerTokenResolver;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    // private final OAuth2LoginFailureHandler oauth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable);

        http.addFilterBefore(
                apiKeyAuthenticationFilter,
                BearerTokenAuthenticationFilter.class
        );

        http.addFilterAfter(
            userMdcFilter(),
            BearerTokenAuthenticationFilter.class
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                .anyRequest().authenticated());

        http.oauth2Login(
            oauth2Login -> oauth2Login
            .successHandler(oauth2LoginSuccessHandler)
            // .failureHandler(oauth2LoginFailureHandler)
        );

        http.logout(AbstractHttpConfigurer::disable);

        http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(
                        jwtConfigurer -> jwtConfigurer
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .bearerTokenResolver(jwtBearerTokenResolver)
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );
        return http.build();
    }

    @Bean
    public UserMdcFilter userMdcFilter() {
        return new UserMdcFilter();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CustomAuthoritiesConverter());
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
