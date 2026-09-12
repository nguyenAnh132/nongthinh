package com.nongthinh.file_service.application.port.in.file;

import java.util.List;
import com.nongthinh.file_service.application.view.FileView;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public interface ListMyFilesUseCase {

    List<FileView> execute(String purpose);
}
