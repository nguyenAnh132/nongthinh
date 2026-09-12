package com.nongthinh.post_service.application.port.in.posttype.impl;

import com.nongthinh.post_service.application.port.in.posttype.DeletePostTypeUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePostTypeUseCaseImpl implements DeletePostTypeUseCase {
    private final PostTypeUseCaseSupport support;
    private final PostTypeRepository repository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void execute(UUID id) {
        support.require(id);
        if (postRepository.existsByPostTypeId(id)) {
            throw new BusinessException(ErrorCode.POST_TYPE_IN_USE);
        }
        repository.deleteById(id);
    }
}
