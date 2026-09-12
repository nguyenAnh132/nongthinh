package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.postmedia.CreatePostMediaUseCase;
import com.nongthinh.post_service.application.port.in.postmedia.DeletePostMediaUseCase;
import com.nongthinh.post_service.application.port.in.postmedia.GetPostMediaUseCase;
import com.nongthinh.post_service.application.port.in.postmedia.ListPostMediaUseCase;
import com.nongthinh.post_service.application.port.in.postmedia.UpdatePostMediaUseCase;
import com.nongthinh.post_service.application.view.PostMediaView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.presentation.dto.request.CreatePostMediaRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostMediaRequest;
import com.nongthinh.post_service.presentation.mapper.PostMediaMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("{postId}/media")
@RequiredArgsConstructor
public class PostMediaController {
    private final ListPostMediaUseCase listPostMediaUseCase;
    private final GetPostMediaUseCase getPostMediaUseCase;
    private final CreatePostMediaUseCase createPostMediaUseCase;
    private final UpdatePostMediaUseCase updatePostMediaUseCase;
    private final DeletePostMediaUseCase deletePostMediaUseCase;
    private final PostMediaMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostMediaView>>> listPostMedia(
            @PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.<List<PostMediaView>>builder()
                .message("Post media retrieved successfully")
                .result(listPostMediaUseCase.execute(postId)).build());
    }

    @GetMapping("{mediaId}")
    public ResponseEntity<ApiResponse<PostMediaView>> getPostMedia(
            @PathVariable UUID postId, @PathVariable UUID mediaId) {
        return ResponseEntity.ok(ApiResponse.<PostMediaView>builder()
                .message("Post media retrieved successfully")
                .result(getPostMediaUseCase.execute(postId, mediaId)).build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostMediaView>> createPostMedia(
            @PathVariable UUID postId, @RequestBody @Valid CreatePostMediaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PostMediaView>builder()
                .message("Post media created successfully")
                .result(createPostMediaUseCase.execute(postId, mapper.toCommand(request))).build());
    }

    @PutMapping("{mediaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostMediaView>> updatePostMedia(
            @PathVariable UUID postId, @PathVariable UUID mediaId,
            @RequestBody @Valid UpdatePostMediaRequest request) {
        return ResponseEntity.ok(ApiResponse.<PostMediaView>builder()
                .message("Post media updated successfully")
                .result(updatePostMediaUseCase.execute(
                        postId, mediaId, mapper.toCommand(request))).build());
    }

    @DeleteMapping("{mediaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<Void>> deletePostMedia(
            @PathVariable UUID postId, @PathVariable UUID mediaId) {
        deletePostMediaUseCase.execute(postId, mediaId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Post media deleted successfully").build());
    }
}
