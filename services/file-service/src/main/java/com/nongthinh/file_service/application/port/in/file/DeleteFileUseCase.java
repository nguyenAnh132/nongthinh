package com.nongthinh.file_service.application.port.in.file;

import java.util.UUID;

public interface DeleteFileUseCase {

    void execute(UUID fileId);
}
