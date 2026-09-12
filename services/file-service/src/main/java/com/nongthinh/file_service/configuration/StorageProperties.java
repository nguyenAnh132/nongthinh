package com.nongthinh.file_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String bucket = "nongthinh-files";

    private String publicBaseUrl = "http://localhost:9095/files/public";

    private String basePath = "./storage";
}
