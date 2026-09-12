package com.nongthinh.post_service.presentation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nongthinh.post_service.application.port.in.postbookmark.GetPostBookmarkStatusUseCase;
import com.nongthinh.post_service.application.port.in.postbookmark.ListMyBookmarkedPostsUseCase;
import com.nongthinh.post_service.application.port.in.postbookmark.RemovePostBookmarkUseCase;
import com.nongthinh.post_service.application.port.in.postbookmark.SavePostBookmarkUseCase;
import com.nongthinh.post_service.application.port.in.postshare.CreatePostShareUseCase;
import com.nongthinh.post_service.application.port.in.postshare.GetPostShareSummaryUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostBookmarkStatusView;
import com.nongthinh.post_service.application.view.PostBookmarkView;
import com.nongthinh.post_service.application.view.PostShareSummaryView;
import com.nongthinh.post_service.application.view.PostShareView;
import java.time.Instant;
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
class PostInteractionControllersTest {
    private static final UUID POST_ID = UUID.fromString("9b909490-bf1d-4303-93ca-7456cf16b3af");
    private static final UUID USER_ID = UUID.fromString("e78115a2-03ce-487b-a3ea-e2f661680e05");
    private static final UUID SHARE_ID = UUID.fromString("95520518-8638-4aec-a288-20a52bee34cd");
    private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");

    @Mock private ListMyBookmarkedPostsUseCase listMyBookmarkedPostsUseCase;
    @Mock private GetPostBookmarkStatusUseCase getPostBookmarkStatusUseCase;
    @Mock private SavePostBookmarkUseCase savePostBookmarkUseCase;
    @Mock private RemovePostBookmarkUseCase removePostBookmarkUseCase;
    @Mock private GetPostShareSummaryUseCase getPostShareSummaryUseCase;
    @Mock private CreatePostShareUseCase createPostShareUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PostBookmarkController bookmarkController = new PostBookmarkController(
                listMyBookmarkedPostsUseCase, getPostBookmarkStatusUseCase,
                savePostBookmarkUseCase, removePostBookmarkUseCase);
        PostShareController shareController = new PostShareController(
                getPostShareSummaryUseCase, createPostShareUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(bookmarkController, shareController).build();
    }

    @Test
    void listsCurrentUsersBookmarkedPosts() throws Exception {
        when(listMyBookmarkedPostsUseCase.execute(0, 20))
                .thenReturn(new PageView<>(List.of(), 0, 20, 0, 0, false));

        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Bookmarked posts retrieved successfully"))
                .andExpect(jsonPath("$.result.items").isArray());
    }

    @Test
    void getsAndSavesBookmarkAtNestedResource() throws Exception {
        when(getPostBookmarkStatusUseCase.execute(POST_ID))
                .thenReturn(new PostBookmarkStatusView(POST_ID, true));
        when(savePostBookmarkUseCase.execute(POST_ID))
                .thenReturn(new PostBookmarkView(POST_ID, USER_ID, NOW));

        mockMvc.perform(get("/{postId}/bookmarks", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.bookmarked").value(true));
        mockMvc.perform(put("/{postId}/bookmarks", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userId").value(USER_ID.toString()));
    }

    @Test
    void removesBookmarkIdempotently() throws Exception {
        mockMvc.perform(delete("/{postId}/bookmarks", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post bookmark removed successfully"));

        verify(removePostBookmarkUseCase).execute(POST_ID);
    }

    @Test
    void getsShareSummaryAndCreatesShare() throws Exception {
        when(getPostShareSummaryUseCase.execute(POST_ID))
                .thenReturn(new PostShareSummaryView(POST_ID, 4));
        when(createPostShareUseCase.execute(POST_ID))
                .thenReturn(new PostShareView(SHARE_ID, POST_ID, USER_ID, NOW));

        mockMvc.perform(get("/{postId}/shares", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalCount").value(4));
        mockMvc.perform(post("/{postId}/shares", POST_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.id").value(SHARE_ID.toString()));
    }
}
