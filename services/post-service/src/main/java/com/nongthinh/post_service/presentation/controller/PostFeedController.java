package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.command.GetPostFeedCommand;
import com.nongthinh.post_service.application.port.in.post.GetPostFeedUseCase;
import com.nongthinh.post_service.application.view.PostFeedView;
import com.nongthinh.post_service.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostFeedController {
    private final GetPostFeedUseCase useCase;

    @GetMapping("/feed")
    public ApiResponse<PostFeedView> getFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID postTypeId,
            @RequestParam(required = false) UUID topicId,
            @RequestParam(required = false) UUID cropTypeId,
            @RequestParam(required = false) UUID authorUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean followingOnly) {
        return ApiResponse.<PostFeedView>builder().message("Feed retrieved successfully")
                .result(useCase.execute(new GetPostFeedCommand(cursor, postTypeId, topicId,
                        cropTypeId, authorUserId, keyword, followingOnly))).build();
    }
}
