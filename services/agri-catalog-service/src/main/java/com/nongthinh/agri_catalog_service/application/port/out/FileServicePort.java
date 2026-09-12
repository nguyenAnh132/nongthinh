package com.nongthinh.agri_catalog_service.application.port.out;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.model.FileMetadata;

public interface FileServicePort {

    FileMetadata getActiveFile(UUID fileId);
}
