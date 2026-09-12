package com.nongthinh.post_service.application.port.out;

import com.nongthinh.post_service.application.model.FileMetadata;
import java.util.UUID;

public interface FileQuery {
    FileMetadata getById(UUID fileId);
}
