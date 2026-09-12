package com.nongthinh.auth_service.infra.client.boportal;

import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.infra.client.boportal.dto.SystemParamValueResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bo-portal-client", url = "${spring.security.api-key.clients.bo-portal-service.url}")
public interface BoPortalClient {

    @GetMapping("/internal/system-params/{name}")
    ApiResponse<SystemParamValueResponse> getSystemParam(
            @PathVariable("name") String name,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
