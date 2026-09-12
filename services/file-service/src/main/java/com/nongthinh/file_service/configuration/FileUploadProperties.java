package com.nongthinh.file_service.configuration;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadProperties {

    private Map<FilePurpose, Long> maxSizeBytes = Map.of(
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
}
