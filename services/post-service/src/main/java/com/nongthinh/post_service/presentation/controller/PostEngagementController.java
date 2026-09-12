package com.nongthinh.post_service.presentation.controller;
import com.nongthinh.post_service.application.port.in.post.GetPostEngagementUseCase;
import com.nongthinh.post_service.application.view.PostEngagementView;
import com.nongthinh.post_service.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
public class PostEngagementController {
    private final GetPostEngagementUseCase useCase;
    @GetMapping("{postId}/engagement")
    public ApiResponse<PostEngagementView> get(@PathVariable UUID postId) {
        return ApiResponse.<PostEngagementView>builder().message("Post engagement retrieved successfully")
                .result(useCase.execute(postId)).build();
    }
}
