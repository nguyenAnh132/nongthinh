package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.postreport.GetPostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.ListPostReportsUseCase;
import com.nongthinh.post_service.application.port.in.postreport.RejectPostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.ResolvePostReportUseCase;
import com.nongthinh.post_service.application.port.in.postreport.StartPostReportReviewUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import com.nongthinh.post_service.presentation.dto.request.CompletePostReportRequest;
import com.nongthinh.post_service.presentation.mapper.PostReportMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/post-reports")
@PreAuthorize("hasAuthority('content:moderate')")
@Validated
@RequiredArgsConstructor
public class AdminPostReportController {
    private final ListPostReportsUseCase listPostReportsUseCase;
    private final GetPostReportUseCase getPostReportUseCase;
    private final StartPostReportReviewUseCase startPostReportReviewUseCase;
    private final ResolvePostReportUseCase resolvePostReportUseCase;
    private final RejectPostReportUseCase rejectPostReportUseCase;
    private final PostReportMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageView<PostReportView>>> listPostReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageView<PostReportView>>builder()
                .message("Post reports retrieved successfully")
                .result(listPostReportsUseCase.execute(status, page, size))
                .build());
    }

    @GetMapping("{reportId}")
    public ResponseEntity<ApiResponse<PostReportView>> getPostReport(
            @PathVariable UUID reportId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostReportView>builder()
                .message("Post report retrieved successfully")
                .result(getPostReportUseCase.execute(reportId))
                .build());
    }

    @PostMapping("{reportId}/review")
    public ResponseEntity<ApiResponse<PostReportView>> startPostReportReview(
            @PathVariable UUID reportId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostReportView>builder()
                .message("Post report review started successfully")
                .result(startPostReportReviewUseCase.execute(reportId))
                .build());
    }

    @PostMapping("{reportId}/resolve")
    public ResponseEntity<ApiResponse<PostReportView>> resolvePostReport(
            @PathVariable UUID reportId,
            @RequestBody @Valid CompletePostReportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<PostReportView>builder()
                .message("Post report resolved successfully")
                .result(resolvePostReportUseCase.execute(
                        reportId,
                        mapper.toCompletePostReportCommand(request)
                ))
                .build());
    }

    @PostMapping("{reportId}/reject")
    public ResponseEntity<ApiResponse<PostReportView>> rejectPostReport(
            @PathVariable UUID reportId,
            @RequestBody @Valid CompletePostReportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<PostReportView>builder()
                .message("Post report rejected successfully")
                .result(rejectPostReportUseCase.execute(
                        reportId,
                        mapper.toCompletePostReportCommand(request)
                ))
                .build());
    }
}
