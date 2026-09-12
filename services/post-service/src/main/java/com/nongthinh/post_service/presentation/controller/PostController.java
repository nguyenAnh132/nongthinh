package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.post.CreatePostUseCase;
import com.nongthinh.post_service.application.port.in.post.DeletePostUseCase;
import com.nongthinh.post_service.application.port.in.post.GetPostUseCase;
import com.nongthinh.post_service.application.port.in.post.ListMyPostsUseCase;
import com.nongthinh.post_service.application.port.in.post.ListPublicPostsUseCase;
import com.nongthinh.post_service.application.port.in.post.PublishPostUseCase;
import com.nongthinh.post_service.application.port.in.post.UpdatePostUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.presentation.dto.request.CreatePostRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostRequest;
import com.nongthinh.post_service.presentation.mapper.PostMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class PostController {
    private final ListPublicPostsUseCase listPublicPostsUseCase;
    private final ListMyPostsUseCase listMyPostsUseCase;
    private final GetPostUseCase getPostUseCase;
    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final PublishPostUseCase publishPostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final PostMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PageView<PostView>>> listPublicPosts(
            @RequestParam(required = false) UUID postTypeId,
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) UUID cropTypeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        PageView<PostView> result = listPublicPostsUseCase.execute(
                postTypeId, topicId, cropTypeId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.<PageView<PostView>>builder()
                .message("Posts retrieved successfully").result(result).build());
    }

    @GetMapping("me")
    public ResponseEntity<ApiResponse<PageView<PostView>>> listMyPosts(
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        PageView<PostView> result = listMyPostsUseCase.execute(status, page, size);
        return ResponseEntity.ok(ApiResponse.<PageView<PostView>>builder()
                .message("My posts retrieved successfully").result(result).build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<PostView>> getPost(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PostView>builder()
                .message("Post retrieved successfully").result(getPostUseCase.execute(id)).build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostView>> createPost(
            @RequestBody @Valid CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PostView>builder()
                .message("Post created successfully")
                .result(createPostUseCase.execute(mapper.toCommand(request))).build());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostView>> updatePost(
            @PathVariable UUID id, @RequestBody @Valid UpdatePostRequest request) {
        return ResponseEntity.ok(ApiResponse.<PostView>builder()
                .message("Post updated successfully")
                .result(updatePostUseCase.execute(id, mapper.toCommand(request))).build());
    }

    @PostMapping("{id}/publish")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostView>> publishPost(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PostView>builder()
                .message("Post published successfully").result(publishPostUseCase.execute(id)).build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable UUID id) {
        deletePostUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Post deleted successfully").build());
    }
}
