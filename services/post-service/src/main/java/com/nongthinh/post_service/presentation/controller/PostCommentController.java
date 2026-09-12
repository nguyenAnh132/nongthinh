package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.view.DeletedPostCommentView;

import com.nongthinh.post_service.application.port.in.postcomment.CreatePostCommentUseCase;
import com.nongthinh.post_service.application.port.in.postcomment.DeletePostCommentUseCase;
import com.nongthinh.post_service.application.port.in.postcomment.ListPostCommentsUseCase;
import com.nongthinh.post_service.application.port.in.postcomment.UpdatePostCommentUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostCommentThreadView;
import com.nongthinh.post_service.application.view.PostCommentView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.presentation.dto.request.CreatePostCommentRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostCommentRequest;
import com.nongthinh.post_service.presentation.mapper.PostCommentMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("{postId}/comments")
@Validated
@RequiredArgsConstructor
public class PostCommentController {
    private final ListPostCommentsUseCase listPostCommentsUseCase;
    private final CreatePostCommentUseCase createPostCommentUseCase;
    private final UpdatePostCommentUseCase updatePostCommentUseCase;
    private final DeletePostCommentUseCase deletePostCommentUseCase;
    private final PostCommentMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageView<PostCommentThreadView>>> listPostComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageView<PostCommentThreadView>>builder()
                .message("Post comments retrieved successfully")
                .result(listPostCommentsUseCase.execute(postId, page, size)).build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostCommentView>> createPostComment(
            @PathVariable UUID postId,
            @RequestBody @Valid CreatePostCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PostCommentView>builder()
                        .message("Post comment created successfully")
                        .result(createPostCommentUseCase.execute(postId, mapper.toCommand(request)))
                        .build());
    }

    @PutMapping("{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostCommentView>> updatePostComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody @Valid UpdatePostCommentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<PostCommentView>builder()
                .message("Post comment updated successfully")
                .result(updatePostCommentUseCase.execute(
                        postId, commentId, mapper.toCommand(request)))
                .build());
    }

    @DeleteMapping("{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<DeletedPostCommentView>> deletePostComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId
    ) {
        return ResponseEntity.ok(ApiResponse.<DeletedPostCommentView>builder()
                .result(deletePostCommentUseCase.execute(postId, commentId))
                .message("Post comment deleted successfully").build());
    }
}
