package com.nongthinh.post_service.application.port.in.posttype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.CreatePostTypeCommand;
import com.nongthinh.post_service.application.command.UpdatePostTypeCommand;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostTypeUseCasesTest {
    private static final UUID ID = UUID.fromString("32bc9302-2b1c-4e23-aa96-0ec0e5da9508");
    private static final Instant NOW = Instant.parse("2026-08-22T10:00:00Z");

    @Mock private PostTypeRepository repository;
    @Mock private PostRepository postRepository;
    @Mock private IdGenerator idGenerator;
    @Mock private ClockProvider clockProvider;
    private PostTypeUseCaseSupport support;

    @BeforeEach
    void setUp() {
        support = new PostTypeUseCaseSupport(repository);
    }

    @Test
    void createsPostTypeAndNormalizesBlankDescription() {
        when(idGenerator.generate()).thenReturn(ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var view = new CreatePostTypeUseCaseImpl(repository, idGenerator, clockProvider)
                .execute(new CreatePostTypeCommand("QUESTION", "Hỏi đáp", "  ", 10));

        assertEquals(ID, view.id());
        assertEquals("QUESTION", view.code());
        assertEquals(null, view.description());
    }

    @Test
    void rejectsDuplicatedCode() {
        when(repository.findByCode("QUESTION")).thenReturn(Optional.of(postType()));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> new CreatePostTypeUseCaseImpl(repository, idGenerator, clockProvider)
                        .execute(new CreatePostTypeCommand("QUESTION", "Hỏi đáp", null, 10)));

        assertEquals(ErrorCode.POST_TYPE_CODE_ALREADY_EXISTS, exception.getErrorCode());
        verify(repository, never()).save(any());
    }

    @Test
    void updatesAndDeactivatesPostType() {
        when(repository.findById(ID)).thenReturn(Optional.of(postType()));
        when(clockProvider.now()).thenReturn(NOW.plusSeconds(60));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var view = new UpdatePostTypeUseCaseImpl(support, repository, clockProvider)
                .execute(ID, new UpdatePostTypeCommand("Câu hỏi", null, 20, false));

        assertEquals("Câu hỏi", view.name());
        assertEquals(20, view.displayOrder());
        assertFalse(view.active());
    }

    @Test
    void preventsDeletingPostTypeUsedByPosts() {
        when(repository.findById(ID)).thenReturn(Optional.of(postType()));
        when(postRepository.existsByPostTypeId(ID)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> new DeletePostTypeUseCaseImpl(support, repository, postRepository).execute(ID));

        assertEquals(ErrorCode.POST_TYPE_IN_USE, exception.getErrorCode());
        verify(repository, never()).deleteById(ID);
    }

    private PostType postType() {
        return PostType.create(ID, "QUESTION", "Hỏi đáp", null, 10, NOW);
    }
}
