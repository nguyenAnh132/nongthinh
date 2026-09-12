package com.nongthinh.post_service.presentation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nongthinh.post_service.application.port.in.posttopic.ListPostTopicsUseCase;
import com.nongthinh.post_service.application.port.in.posttopic.ListTrendingPostTopicsUseCase;
import com.nongthinh.post_service.application.port.in.posttype.ListPostTypesUseCase;
import com.nongthinh.post_service.application.view.TrendingPostTopicView;
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
class PostCatalogControllerTest {
    private static final UUID TOPIC_ID =
            UUID.fromString("0cb18a40-2702-4f62-ad62-d30b80fb0e11");

    @Mock private ListPostTypesUseCase listPostTypesUseCase;
    @Mock private ListPostTopicsUseCase listPostTopicsUseCase;
    @Mock private ListTrendingPostTopicsUseCase listTrendingPostTopicsUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PostCatalogController(
                listPostTypesUseCase, listPostTopicsUseCase, listTrendingPostTopicsUseCase)).build();
    }

    @Test
    void listsTrendingTopicsWithDefaultLimit() throws Exception {
        when(listTrendingPostTopicsUseCase.execute(3)).thenReturn(List.of(
                new TrendingPostTopicView(1, TOPIC_ID, "Rice diseases", "rice-diseases", 12)
        ));

        mockMvc.perform(get("/post-topics/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].rank").value(1))
                .andExpect(jsonPath("$.result[0].id").value(TOPIC_ID.toString()))
                .andExpect(jsonPath("$.result[0].postCount").value(12));

        verify(listTrendingPostTopicsUseCase).execute(3);
    }
}
