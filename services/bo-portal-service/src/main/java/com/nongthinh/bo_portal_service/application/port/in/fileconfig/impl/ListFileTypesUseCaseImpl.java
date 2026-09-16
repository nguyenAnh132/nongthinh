package com.nongthinh.bo_portal_service.application.port.in.fileconfig.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.nongthinh.bo_portal_service.application.port.in.fileconfig.ListFileTypesUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.FilePurposeTypeRepository;
import com.nongthinh.bo_portal_service.application.view.FileTypeView;

@Service
@RequiredArgsConstructor
public class ListFileTypesUseCaseImpl implements ListFileTypesUseCase {
    private final FilePurposeTypeRepository repository;

    @Override
    public List<FileTypeView> execute() {
        return repository.findAllFileTypes().stream().map(FileTypeView::from).toList();
    }
}
