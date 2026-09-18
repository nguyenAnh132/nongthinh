package com.nongthinh.bo_portal_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurpose;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurposeType;
import com.nongthinh.bo_portal_service.domain.fileconfig.FileType;

public interface FilePurposeTypeRepository {
    List<FileType> findAllFileTypes();
    List<FilePurposeType> findByPurpose(FilePurpose purpose);
    Optional<FilePurposeType> findByPurposeAndTypeCode(FilePurpose purpose, String typeCode);
    FilePurposeType save(FilePurposeType filePurposeType);
}
