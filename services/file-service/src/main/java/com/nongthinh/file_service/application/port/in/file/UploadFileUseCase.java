package com.nongthinh.file_service.application.port.in.file;

import com.nongthinh.file_service.application.command.UploadFileCommand;
import com.nongthinh.file_service.application.view.FileView;

public interface UploadFileUseCase {

    FileView execute(UploadFileCommand command);
}
