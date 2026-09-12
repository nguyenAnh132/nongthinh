package com.nongthinh.post_service.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nongthinh.post_service.application.command.CompletePostReportCommand;
import com.nongthinh.post_service.application.command.CreatePostReportCommand;
import com.nongthinh.post_service.application.port.in.postreport.CreatePostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.GetPostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.ListPostReportsUseCase;
import com.nongthinh.post_service.application.port.in.postreport.RejectPostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.ResolvePostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.StartPostReportReviewUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;
import com.nongthinh.post_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.post_service.presentation.dto.request.CompletePostReportRequest;
import com.nongthinh.post_service.presentation.dto.request.CreatePostReportRequest;
import com.nongthinh.post_service.presentation.mapper.PostReportMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PostReportControllersTest {
    private static final UUID POST_ID = UUID.fromString("2d68a1db-87a8-4402-b559-9e2183215c6f");
    private static final UUID REPORT_ID = UUID.fromString("0b7b4eb8-5180-4659-b8af-a693c30ab52c");
    private static final UUID REPORTER_ID = UUID.fromString("b09f8346-f81e-4576-9ad9-df67f07af8a3");
    private static final UUID MODERATOR_ID = UUID.fromString("956319bb-26e8-4889-8843-a9507f27c101");
    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Mock private CreatePostReportUseCase createPostReportUseCase;
    @Mock private ListPostReportsUseCase listPostReportsUseCase;
    @Mock private GetPostReportUseCase getPostReportUseCase;
    @Mock private StartPostReportReviewUseCase startPostReportReviewUseCase;
    @Mock private ResolvePostReportUseCase resolvePostReportUseCase;
    @Mock private RejectPostReportUseCase rejectPostReportUseCase;
    @Mock private PostReportMapper mapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PostReportController userController = new PostReportController(
                createPostReportUseCase, mapper
        );
        AdminPostReportController adminController = new AdminPostReportController(
                listPostReportsUseCase,
                getPostReportUseCase,
                startPostReportReviewUseCase,
                resolvePostReportUseCase,
                rejectPostReportUseCase,
                mapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(userController, adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsPostReportAtNestedResource() throws Exception {
        CreatePostReportCommand command = new CreatePostReportCommand(
                ReportReason.SPAM, "Repeated ads"
        );
        when(mapper.toCreatePostReportCommand(any(CreatePostReportRequest.class)))
                .thenReturn(command);
        when(createPostReportUseCase.execute(POST_ID, command)).thenReturn(pendingView());

        mockMvc.perform(post("/{postId}/reports", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"SPAM","reasonDetail":"Repeated ads"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Post report created successfully"))
                .andExpect(jsonPath("$.result.id").value(REPORT_ID.toString()))
                .andExpect(jsonPath("$.result.status").value("PENDING"));
    }

    @Test
    void rejectsMissingReportReasonAtHttpBoundary() throws Exception {
        mockMvc.perform(post("/{postId}/reports", POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_POST_REPORT_REASON_REQUIRED"));
    }

    @Test
    void listsAndGetsReportsForModeration() throws Exception {
        when(listPostReportsUseCase.execute(ReportStatus.PENDING, 0, 20))
                .thenReturn(new PageView<>(List.of(pendingView()), 0, 20, 1, 1, false));
        when(getPostReportUseCase.execute(REPORT_ID)).thenReturn(pendingView());

        mockMvc.perform(get("/admin/post-reports")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items[0].id").value(REPORT_ID.toString()));

        mockMvc.perform(get("/admin/post-reports/{reportId}", REPORT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reason").value("SPAM"));
    }

    @Test
    void startsReviewAndCompletesReport() throws Exception {
        PostReportView underReview = view(ReportStatus.UNDER_REVIEW, null, null);
        PostReportView resolved = view(
                ReportStatus.RESOLVED, MODERATOR_ID, "Confirmed violation"
        );
        CompletePostReportCommand command = new CompletePostReportCommand(
                "Confirmed violation", PostModerationAction.HIDE
        );
        when(startPostReportReviewUseCase.execute(REPORT_ID)).thenReturn(underReview);
        when(mapper.toCompletePostReportCommand(any(CompletePostReportRequest.class)))
                .thenReturn(command);
        when(resolvePostReportUseCase.execute(eq(REPORT_ID), eq(command))).thenReturn(resolved);

        mockMvc.perform(post("/admin/post-reports/{reportId}/review", REPORT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("UNDER_REVIEW"));

        mockMvc.perform(post("/admin/post-reports/{reportId}/resolve", REPORT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolutionNote\":\"Confirmed violation\",\"moderationAction\":\"HIDE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("RESOLVED"))
                .andExpect(jsonPath("$.result.resolvedBy").value(MODERATOR_ID.toString()));
    }

    private static PostReportView pendingView() {
        return view(ReportStatus.PENDING, null, null);
    }

    private static PostReportView view(
            ReportStatus status,
            UUID resolvedBy,
            String resolutionNote
    ) {
        Instant resolvedAt = resolvedBy == null ? null : NOW.plusSeconds(1);
        return new PostReportView(
                REPORT_ID,
                POST_ID,
                REPORTER_ID,
                ReportReason.SPAM,
                "Repeated ads",
                status,
                resolvedBy,
                resolvedAt,
                resolutionNote,
                NOW,
                resolvedAt == null ? NOW : resolvedAt
        );
    }
}
