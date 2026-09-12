package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.postshare.CreatePostShareUseCase;
import com.nongthinh.post_service.application.port.in.postshare.GetPostShareSummaryUseCase;
import com.nongthinh.post_service.application.view.PostShareSummaryView;
import com.nongthinh.post_service.application.view.PostShareView;
import com.nongthinh.post_service.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("{postId}/shares")
@RequiredArgsConstructor
public class PostShareController {
    private final GetPostShareSummaryUseCase getPostShareSummaryUseCase;
    private final CreatePostShareUseCase createPostShareUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PostShareSummaryView>> getPostShareSummary(
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostShareSummaryView>builder()
                .message("Post shares retrieved successfully")
                .result(getPostShareSummaryUseCase.execute(postId)).build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostShareView>> createPostShare(
            @PathVariable UUID postId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PostShareView>builder()
                        .message("Post share created successfully")
                        .result(createPostShareUseCase.execute(postId)).build());
    }
}
