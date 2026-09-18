package com.nongthinh.file_service.configuration;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.upload.default")
public class FileUploadProperties {

    @NotNull
    private Map<FilePurpose, @NotNull @Positive Long> maxSizeBytes = Map.of(
            FilePurpose.AVATAR, 2L * 1024 * 1024,
            FilePurpose.BRAND_LOGO, 2L * 1024 * 1024,
            FilePurpose.BRAND_BANNER, 5L * 1024 * 1024,
            FilePurpose.BUSINESS_LICENSE, 10L * 1024 * 1024,
            FilePurpose.PRODUCT_IMAGE, 5L * 1024 * 1024,
            FilePurpose.DISEASE_IMAGE, 5L * 1024 * 1024,
            FilePurpose.DIAGNOSIS_IMAGE, 5L * 1024 * 1024,
            FilePurpose.POST_IMAGE, 5L * 1024 * 1024,
            FilePurpose.POST_VIDEO, 50L * 1024 * 1024,
            FilePurpose.MODEL_ARTIFACT, 50L * 1024 * 1024);

    public long maxSizeFor(FilePurpose purpose) {
        Long size = maxSizeBytes.get(purpose);
        if (size == null || size <= 0) {
            throw new IllegalStateException("Missing or invalid default upload size for " + purpose);
        }
        return size;
    }
}
