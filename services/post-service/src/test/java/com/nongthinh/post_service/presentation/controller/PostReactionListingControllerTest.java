package com.nongthinh.post_service.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nongthinh.post_service.application.port.in.postreaction.GetPostReactionSummaryUseCase;
import com.nongthinh.post_service.application.port.in.postreaction.ListPostReactionsUseCase;
import com.nongthinh.post_service.application.port.in.postreaction.RemovePostReactionUseCase;
import com.nongthinh.post_service.application.port.in.postreaction.SetPostReactionUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.post_service.presentation.mapper.PostReactionMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PostReactionListingControllerTest {
    private static final UUID POST_ID = UUID.fromString("78bab3eb-6613-42c3-9d95-e8be298ef242");
    @Mock private ListPostReactionsUseCase list;
    @Mock private GetPostReactionSummaryUseCase summary;
    @Mock private SetPostReactionUseCase set;
    @Mock private RemovePostReactionUseCase remove;
    @Mock private PostReactionMapper mapper;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new PostReactionController(summary, list, set, remove, mapper))
                .setControllerAdvice(new GlobalExceptionHandler(java.util.Optional.empty())).build();
    }

    @Test
    void exposesPaginatedUsersAtSeparateResource() throws Exception {
        when(list.execute(POST_ID, com.nongthinh.post_service.domain.interaction.valueobject.ReactionType.LOVE, 2, 10))
                .thenReturn(new PageView<>(List.of(), 2, 10, 20, 2, false));
        mvc.perform(get("/{id}/reactions/users", POST_ID).param("reactionType", "LOVE")
                        .param("page", "2").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.page").value(2))
                .andExpect(jsonPath("$.result.totalElements").value(20));
    }

    @Test
    void mapsUnavailablePostToNotFound() throws Exception {
        when(list.execute(POST_ID, null, 0, 20)).thenThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));
        mvc.perform(get("/{id}/reactions/users", POST_ID)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidPageAndIdentifier() throws Exception {
        when(list.execute(POST_ID, null, -1, 20)).thenThrow(new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER));
        mvc.perform(get("/{id}/reactions/users", POST_ID).param("page", "-1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/not-a-uuid/reactions/users")).andExpect(status().isBadRequest());
        mvc.perform(get("/{id}/reactions/users", POST_ID).param("reactionType", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}
