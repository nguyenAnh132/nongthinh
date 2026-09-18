package com.nongthinh.bo_portal_service.application.port.in.fileconfig;

import com.nongthinh.bo_portal_service.application.view.FilePurposeTypeView;
import com.nongthinh.bo_portal_service.application.command.UpdateFilePurposeTypeCommand;

public interface UpdateFilePurposeTypeUseCase {
    FilePurposeTypeView execute(String purpose, String typeCode, UpdateFilePurposeTypeCommand command);
}
