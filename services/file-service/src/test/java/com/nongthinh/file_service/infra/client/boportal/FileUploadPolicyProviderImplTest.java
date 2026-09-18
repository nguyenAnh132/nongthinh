package com.nongthinh.file_service.infra.client.boportal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import com.nongthinh.file_service.common.response.ApiResponse;
import com.nongthinh.file_service.configuration.FileUploadProperties;
import com.nongthinh.file_service.domain.file.FileValidationPolicy;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.infra.client.boportal.dto.FileUploadPolicyDto;

class FileUploadPolicyProviderImplTest {
    private final BoPortalClient client = mock(BoPortalClient.class);
    private final FileUploadProperties defaults = new FileUploadProperties();
    private final FileUploadPolicyProviderImpl provider =
            new FileUploadPolicyProviderImpl(client, defaults, "test-key");

    @Test
    void boValuesOverrideDefaultsIncludingEmptyTypeSet() {
        respond(new FileUploadPolicyDto("AVATAR", 123L, Set.of()));
        var policy = provider.getPolicy(FilePurpose.AVATAR);
        assertEquals(123, policy.maxSizeBytes());
        assertEquals(Set.of(), policy.allowedContentTypes());
        verify(client).getUploadPolicy("AVATAR", "test-key");
    }

    @Test
    void missingSizeUsesConfiguredDefaultWithoutReenablingTypes() {
        defaults.setMaxSizeBytes(Map.of(FilePurpose.AVATAR, 321L));
        respond(new FileUploadPolicyDto("AVATAR", null, Set.of()));
        var policy = provider.getPolicy(FilePurpose.AVATAR);
        assertEquals(321, policy.maxSizeBytes());
        assertTrue(policy.allowedContentTypes().isEmpty());
    }

    @Test
    void missingTypesUseDefaultButKeepBoSize() {
        respond(new FileUploadPolicyDto("AVATAR", 123L, null));
        var policy = provider.getPolicy(FilePurpose.AVATAR);
        assertEquals(123, policy.maxSizeBytes());
        assertEquals(FileValidationPolicy.supportedContentTypes(FilePurpose.AVATAR), policy.allowedContentTypes());
    }

    @Test
    void downstreamFailureOrMissUsesDefaults() {
        when(client.getUploadPolicy("AVATAR", "test-key")).thenThrow(mock(FeignException.class));
        assertEquals(2097152, provider.getPolicy(FilePurpose.AVATAR).maxSizeBytes());
        reset(client);
        when(client.getUploadPolicy("AVATAR", "test-key"))
                .thenReturn(ApiResponse.<FileUploadPolicyDto>builder().result(Optional.empty()).build());
        assertEquals(2097152, provider.getPolicy(FilePurpose.AVATAR).maxSizeBytes());
    }

    @Test
    void invalidSizeFallsBackIndependentlyAndWrongPurposeIsIgnored() {
        respond(new FileUploadPolicyDto("AVATAR", -1L, Set.of()));
        assertEquals(2097152, provider.getPolicy(FilePurpose.AVATAR).maxSizeBytes());
        assertTrue(provider.getPolicy(FilePurpose.AVATAR).allowedContentTypes().isEmpty());
        respond(new FileUploadPolicyDto("POST_VIDEO", 123L, Set.of("video/mp4")));
        var policy = provider.getPolicy(FilePurpose.AVATAR);
        assertEquals(2097152, policy.maxSizeBytes());
        assertEquals(FileValidationPolicy.supportedContentTypes(FilePurpose.AVATAR), policy.allowedContentTypes());
    }

    private void respond(FileUploadPolicyDto dto) {
        when(client.getUploadPolicy("AVATAR", "test-key"))
                .thenReturn(ApiResponse.<FileUploadPolicyDto>builder().result(Optional.of(dto)).build());
    }
}
