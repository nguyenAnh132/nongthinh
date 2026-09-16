package com.nongthinh.bo_portal_service.application.port.in.fileconfig;

import java.util.List;
import com.nongthinh.bo_portal_service.application.view.FileTypeView;

public interface ListFileTypesUseCase {
    List<FileTypeView> execute();
}
