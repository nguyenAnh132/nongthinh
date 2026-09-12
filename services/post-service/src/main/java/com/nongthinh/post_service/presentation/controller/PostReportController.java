package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.postreport.CreatePostReportUseCase;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.presentation.dto.request.CreatePostReportRequest;
import com.nongthinh.post_service.presentation.mapper.PostReportMapper;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("{postId}/reports")
@RequiredArgsConstructor
public class PostReportController {
    private final CreatePostReportUseCase createPostReportUseCase;
    private final PostReportMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostReportView>> createPostReport(
            @PathVariable UUID postId,
            @RequestBody @Valid CreatePostReportRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PostReportView>builder()
                        .message("Post report created successfully")
                        .result(createPostReportUseCase.execute(
                                postId,
                                mapper.toCreatePostReportCommand(request)
                        ))
                        .build());
    }
}
