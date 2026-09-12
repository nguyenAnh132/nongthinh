package com.nongthinh.file_service.application.port.out.storage;

import java.io.InputStream;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;

public interface ObjectStoragePort {

    StorageProvider provider();

    void putObject(String bucket, String objectKey, String contentType, long sizeBytes, InputStream inputStream);

    void deleteObject(String bucket, String objectKey);

    InputStream getObject(String bucket, String objectKey);

    String buildPublicUrl(String objectKey);
}
