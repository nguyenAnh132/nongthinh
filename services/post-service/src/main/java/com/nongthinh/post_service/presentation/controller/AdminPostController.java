package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.post.GetAdminPostUseCase;
import com.nongthinh.post_service.application.port.in.post.GetPostStatisticsUseCase;
import com.nongthinh.post_service.application.port.in.post.ListAdminPostsUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostStatisticsBucket;
import com.nongthinh.post_service.application.view.PostStatisticsView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/posts")
@PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('content:moderate')")
@Validated
@RequiredArgsConstructor
public class AdminPostController {
    private final ListAdminPostsUseCase listAdminPostsUseCase;
    private final GetAdminPostUseCase getAdminPostUseCase;
    private final GetPostStatisticsUseCase getPostStatisticsUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageView<PostView>>> listPosts(
            @RequestParam(required = false) UUID authorUserId,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageView<PostView>>builder()
                .message("Posts retrieved successfully")
                .result(listAdminPostsUseCase.execute(
                        authorUserId, status, keyword, from, to, page, size))
                .build());
    }

    @GetMapping("statistics")
    public ResponseEntity<ApiResponse<PostStatisticsView>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "DAY") PostStatisticsBucket bucket,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timeZone
    ) {
        return ResponseEntity.ok(ApiResponse.<PostStatisticsView>builder()
                .message("Post statistics retrieved successfully")
                .result(getPostStatisticsUseCase.execute(from, to, bucket, ZoneId.of(timeZone)))
                .build());
    }

    @GetMapping("{postId}")
    public ResponseEntity<ApiResponse<PostView>> getPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.<PostView>builder()
                .message("Post retrieved successfully")
                .result(getAdminPostUseCase.execute(postId))
                .build());
    }
}
