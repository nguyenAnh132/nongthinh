package com.nongthinh.post_service.application.port.in.posttype.impl;

import com.nongthinh.post_service.application.port.in.posttype.ListPostTypesUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.application.view.PostTypeView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListPostTypesUseCaseImpl implements ListPostTypesUseCase {
    private final PostTypeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PostTypeView> execute(boolean activeOnly) {
        return (activeOnly ? repository.findAllActive() : repository.findAll())
                .stream().map(PostTypeView::from).toList();
    }
}
