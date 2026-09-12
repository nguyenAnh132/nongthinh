package com.nongthinh.post_service.application.port.in.posttype.impl;

import com.nongthinh.post_service.application.port.in.posttype.GetPostTypeUseCase;
import com.nongthinh.post_service.application.view.PostTypeView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPostTypeUseCaseImpl implements GetPostTypeUseCase {
    private final PostTypeUseCaseSupport support;

    @Override
    @Transactional(readOnly = true)
    public PostTypeView execute(UUID id) {
        return PostTypeView.from(support.require(id));
    }
}
