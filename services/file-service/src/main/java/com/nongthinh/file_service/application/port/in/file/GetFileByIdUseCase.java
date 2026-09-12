package com.nongthinh.file_service.application.port.in.file;

import java.util.UUID;
import com.nongthinh.file_service.application.view.FileView;

public interface GetFileByIdUseCase {

    FileView execute(UUID fileId);
}
