package com.nongthinh.file_service.infra.client.boportal;

import java.util.Set;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.file_service.application.port.out.FileUploadPolicyProvider;
import com.nongthinh.file_service.configuration.FileUploadProperties;
import com.nongthinh.file_service.domain.file.FileUploadPolicy;
import com.nongthinh.file_service.domain.file.FileValidationPolicy;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.infra.client.boportal.dto.FileUploadPolicyDto;

@Component
@Slf4j
public class FileUploadPolicyProviderImpl implements FileUploadPolicyProvider {
    private final BoPortalClient client;
    private final FileUploadProperties defaults;
    private final String apiKey;

    public FileUploadPolicyProviderImpl(BoPortalClient client, FileUploadProperties defaults,
            @Value("${spring.security.api-key.clients.bo-portal-service.api-key}") String apiKey) {
        this.client = client;
        this.defaults = defaults;
        this.apiKey = apiKey;
    }

    @Override
    public FileUploadPolicy getPolicy(FilePurpose purpose) {
        try {
            var response = client.getUploadPolicy(purpose.name(), apiKey);
            if (response != null && "1000".equals(response.getCode())
                    && response.getResult() != null && response.getResult().isPresent()) {
                FileUploadPolicyDto dto = response.getResult().get();
                if (purpose.name().equals(dto.purpose())) {
                    return resolvePolicy(purpose, dto);
                }
            }
            log.warn("[Infra - UploadPolicy] Missing or invalid BO policy, using defaults | purpose={}", purpose);
        } catch (FeignException ex) {
            log.warn("[Infra - UploadPolicy] BO policy unavailable, using defaults | purpose={} status={}",
                    purpose, ex.status());
        }
        return new FileUploadPolicy(defaults.maxSizeFor(purpose), FileValidationPolicy.supportedContentTypes(purpose));
    }

    private FileUploadPolicy resolvePolicy(FilePurpose purpose, FileUploadPolicyDto dto) {
        long size = defaults.maxSizeFor(purpose);
        if (dto.maxSizeBytes() != null && dto.maxSizeBytes() > 0) {
            size = dto.maxSizeBytes();
        } else {
            log.warn("[Infra - UploadPolicy] Missing or invalid BO size, using default | purpose={}", purpose);
        }
        Set<String> types = dto.allowedContentTypes();
        if (types == null) {
            types = FileValidationPolicy.supportedContentTypes(purpose);
        }
        // Explicitly empty means all types disabled; never replace it with defaults.
        return new FileUploadPolicy(size, types);
    }
}
