package com.nongthinh.profile_service.infra.client.location;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.view.AddressNamesView;
import com.nongthinh.profile_service.common.exception.AppException;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.common.response.ApiResponse;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationServicePortImpl implements LocationServicePort {

    private final LocationServiceClient locationServiceClient;

    @Value("${spring.security.api-key.clients.location-service.api-key}")
    private String apiKey;

    @Override
    public void validateAddress(String provinceId, UUID communeId) {
        if (provinceId == null || provinceId.isBlank() || communeId == null) {
            throw new BusinessException(ErrorCode.ADDRESS_LOCATION_INVALID);
        }

        ApiResponse<Boolean> response;
        try {
            response = locationServiceClient.validateAddress(provinceId, communeId, apiKey);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call location-service.validateAddress", ex);
            throw new BusinessException(ErrorCode.LOCATION_SERVICE_UNAVAILABLE, ex);
        }

        boolean valid = response != null
                && response.getResult() != null;
        if (!valid) {
            throw new BusinessException(ErrorCode.ADDRESS_LOCATION_INVALID);
        }
    }

    @Override
    public AddressNamesView resolveAddress(String provinceId, UUID communeId) {
        if (provinceId == null || provinceId.isBlank() || communeId == null) {
            return AddressNamesView.empty();
        }

        try {
            ApiResponse<ResolvedAddressResponse> response =
                    locationServiceClient.resolveAddress(provinceId, communeId, apiKey);
            ResolvedAddressResponse address = response != null ? response.getResult() : null;
            if (address == null) {
                return AddressNamesView.empty();
            }
            return new AddressNamesView(address.provinceName(), address.communeName());
        } catch (Exception ex) {
            log.warn("Could not resolve location names for provinceId={} and communeId={}",
                    provinceId, communeId, ex);
            return AddressNamesView.empty();
        }
    }
}
