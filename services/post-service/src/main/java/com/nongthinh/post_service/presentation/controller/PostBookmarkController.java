package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.postbookmark.GetPostBookmarkStatusUseCase;
import com.nongthinh.post_service.application.port.in.postbookmark.ListMyBookmarkedPostsUseCase;
import com.nongthinh.post_service.application.port.in.postbookmark.RemovePostBookmarkUseCase;
import com.nongthinh.post_service.application.port.in.postbookmark.SavePostBookmarkUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostBookmarkStatusView;
import com.nongthinh.post_service.application.view.PostBookmarkView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.common.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class PostBookmarkController {
    private final ListMyBookmarkedPostsUseCase listMyBookmarkedPostsUseCase;
    private final GetPostBookmarkStatusUseCase getPostBookmarkStatusUseCase;
    private final SavePostBookmarkUseCase savePostBookmarkUseCase;
    private final RemovePostBookmarkUseCase removePostBookmarkUseCase;

    @GetMapping("bookmarks")
    public ResponseEntity<ApiResponse<PageView<PostView>>> listMyBookmarkedPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageView<PostView>>builder()
                .message("Bookmarked posts retrieved successfully")
                .result(listMyBookmarkedPostsUseCase.execute(page, size)).build());
    }

    @GetMapping("{postId}/bookmarks")
    public ResponseEntity<ApiResponse<PostBookmarkStatusView>> getPostBookmarkStatus(
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostBookmarkStatusView>builder()
                .message("Post bookmark status retrieved successfully")
                .result(getPostBookmarkStatusUseCase.execute(postId)).build());
    }

    @PutMapping("{postId}/bookmarks")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostBookmarkView>> savePostBookmark(
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostBookmarkView>builder()
                .message("Post bookmark saved successfully")
                .result(savePostBookmarkUseCase.execute(postId)).build());
    }

    @DeleteMapping("{postId}/bookmarks")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<Void>> removePostBookmark(@PathVariable UUID postId) {
        removePostBookmarkUseCase.execute(postId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Post bookmark removed successfully").build());
    }
}
