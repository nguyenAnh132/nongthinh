package com.nongthinh.profile_service.infra.client.location;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.nongthinh.profile_service.common.response.ApiResponse;

@FeignClient(name = "location-service", url = "${spring.security.api-key.clients.location-service.url}")
public interface LocationServiceClient {

    @GetMapping("/internal/addresses/validate")
    ApiResponse<Boolean> validateAddress(
            @RequestParam("provinceId") String provinceId,
            @RequestParam("communeId") UUID communeId,
            @RequestHeader("X-API-KEY") String apiKey
    );

    @GetMapping("/internal/addresses/resolve")
    ApiResponse<ResolvedAddressResponse> resolveAddress(
            @RequestParam("provinceId") String provinceId,
            @RequestParam("communeId") UUID communeId,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
