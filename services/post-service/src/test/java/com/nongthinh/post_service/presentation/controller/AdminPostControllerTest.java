package com.nongthinh.post_service.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nongthinh.post_service.application.port.in.post.GetAdminPostUseCase;
import com.nongthinh.post_service.application.port.in.post.GetPostStatisticsUseCase;
import com.nongthinh.post_service.application.port.in.post.ListAdminPostsUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostStatisticsBucket;
import com.nongthinh.post_service.application.view.PostStatisticsView;
import com.nongthinh.post_service.application.view.PostTimelinePointView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
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
class AdminPostControllerTest {
    private static final UUID POST_ID = UUID.fromString("cd68bb20-dcd5-4659-8712-45f4ea8550cf");
    private static final UUID AUTHOR_ID = UUID.fromString("83b339ee-23b8-47bd-81d6-d37f81529789");
    private static final UUID TYPE_ID = UUID.fromString("27d58caf-626e-4965-ab0a-d5a7ac88a939");
    private static final Instant FROM = Instant.parse("2026-08-21T17:00:00Z");
    private static final Instant TO = Instant.parse("2026-08-28T17:00:00Z");

    @Mock private ListAdminPostsUseCase listAdminPostsUseCase;
    @Mock private GetAdminPostUseCase getAdminPostUseCase;
    @Mock private GetPostStatisticsUseCase getPostStatisticsUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminPostController(
                listAdminPostsUseCase,
                getAdminPostUseCase,
                getPostStatisticsUseCase
        )).build();
    }

    @Test
    void listsAndReadsCompletePostsForModeration() throws Exception {
        PostView post = postView();
        when(listAdminPostsUseCase.execute(
                AUTHOR_ID, PostStatus.PUBLISHED, "lúa", null, null, 0, 12))
                .thenReturn(new PageView<>(List.of(post), 0, 12, 1, 1, false));
        when(getAdminPostUseCase.execute(POST_ID)).thenReturn(post);

        mockMvc.perform(get("/admin/posts")
                        .param("authorUserId", AUTHOR_ID.toString())
                        .param("status", "PUBLISHED")
                        .param("keyword", "lúa")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.items[0].content").value("Kinh nghiệm chăm lúa"));

        mockMvc.perform(get("/admin/posts/{postId}", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.authorUserId").value(AUTHOR_ID.toString()));
    }

    @Test
    void returnsDailyStatisticsForTheRequestedRange() throws Exception {
        PostStatisticsView statistics = new PostStatisticsView(
                12,
                4,
                FROM,
                TO,
                PostStatisticsBucket.DAY,
                "Asia/Ho_Chi_Minh",
                Map.of(PostStatus.PUBLISHED, 12L),
                List.of(new PostTimelinePointView(FROM, FROM.plusSeconds(86_400), 4))
        );
        when(getPostStatisticsUseCase.execute(
                FROM,
                TO,
                PostStatisticsBucket.DAY,
                ZoneId.of("Asia/Ho_Chi_Minh")
        )).thenReturn(statistics);

        mockMvc.perform(get("/admin/posts/statistics")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("bucket", "DAY")
                        .param("timeZone", "Asia/Ho_Chi_Minh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalPosts").value(12))
                .andExpect(jsonPath("$.result.timeline[0].count").value(4));
    }

    @Test
    void restrictsManagementEndpointsToContentModerators() {
        assertThat(AdminPostController.class.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('ROLE_ADMIN') and hasAuthority('content:moderate')");
    }

    private static PostView postView() {
        return new PostView(
                POST_ID,
                AUTHOR_ID,
                TYPE_ID,
                null,
                "Kinh nghiệm chăm lúa",
                "Đồng Tháp",
                PostVisibility.PUBLIC,
                PostStatus.PUBLISHED,
                List.of(),
                Set.of(),
                FROM,
                FROM,
                FROM,
                null
        );
    }
}
