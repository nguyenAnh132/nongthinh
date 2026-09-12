package com.nongthinh.file_service.application.port.in.file;

import java.util.UUID;
import com.nongthinh.file_service.domain.file.StoredFile;

public interface GetActiveFileForPublicAccessUseCase {

    StoredFile execute(UUID fileId);
}
