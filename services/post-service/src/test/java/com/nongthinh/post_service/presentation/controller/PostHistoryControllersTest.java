package com.nongthinh.post_service.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nongthinh.post_service.application.port.in.posthistory.GetPostHistoryUseCase;
import com.nongthinh.post_service.application.port.in.posthistory.ListPostHistoriesUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostHistoryView;
import com.nongthinh.post_service.application.view.PostSnapshotView;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PostHistoryControllersTest {
    private static final UUID HISTORY_ID = UUID.fromString("84f08874-46fd-4dfd-82d7-0c06960842ee");
    private static final UUID POST_ID = UUID.fromString("e62e1e62-d407-44d6-8adf-32b3cd558ca3");
    private static final UUID POST_TYPE_ID = UUID.fromString("cb5c9d0d-dc73-467e-8220-43561e35cc67");
    private static final UUID AUTHOR_ID = UUID.fromString("1aff301f-54d3-4ba6-a36d-2b8f1b919aa3");
    private static final UUID ACTOR_ID = UUID.fromString("9240b77f-6740-4686-b4a1-50ff1c2b52e5");
    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Mock private ListPostHistoriesUseCase listPostHistoriesUseCase;
    @Mock private GetPostHistoryUseCase getPostHistoryUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AdminPostHistoryController(
                                listPostHistoriesUseCase,
                                getPostHistoryUseCase
                        )
                ).build();
    }

    @Test
    void doesNotExposeAuthorScopedHistoryEndpoints() throws Exception {
        mockMvc.perform(get("/post-histories/me"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/post-histories/me/{historyId}", HISTORY_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void administratorFiltersAndGetsAllPostHistories() throws Exception {
        PostHistoryView view = historyView();
        when(listPostHistoriesUseCase.execute(
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.UPDATED,
                1,
                10
        )).thenReturn(new PageView<>(List.of(view), 1, 10, 11, 2, false));
        when(getPostHistoryUseCase.execute(HISTORY_ID)).thenReturn(view);

        mockMvc.perform(get("/admin/post-histories")
                        .param("postId", POST_ID.toString())
                        .param("postAuthorUserId", AUTHOR_ID.toString())
                        .param("actorUserId", ACTOR_ID.toString())
                        .param("actorType", "ADMIN")
                        .param("action", "UPDATED")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalElements").value(11))
                .andExpect(jsonPath("$.result.items[0].actorType").value("ADMIN"));

        mockMvc.perform(get("/admin/post-histories/{historyId}", HISTORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.postAuthorUserId").value(AUTHOR_ID.toString()));
    }

    @Test
    void restrictsPostHistoryToAdministratorsWithModerationPermission() {
        assertThat(AdminPostHistoryController.class.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ROLE_ADMIN') and hasAuthority('content:moderate')");
    }

    private static PostHistoryView historyView() {
        return new PostHistoryView(
                HISTORY_ID,
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.UPDATED,
                PostStatus.PUBLISHED,
                PostStatus.PUBLISHED,
                PostVisibility.PUBLIC,
                PostVisibility.PUBLIC,
                null,
                null,
                null,
                new PostSnapshotView(
                        "Updated rice advice",
                        POST_TYPE_ID,
                        null,
                        Set.of(),
                        "Đồng Tháp",
                        List.of(),
                        PostStatus.PUBLISHED,
                        PostVisibility.PUBLIC
                ),
                NOW
        );
    }
}
