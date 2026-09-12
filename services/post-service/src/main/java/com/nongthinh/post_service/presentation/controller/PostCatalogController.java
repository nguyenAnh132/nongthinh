package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.posttopic.ListPostTopicsUseCase;
import com.nongthinh.post_service.application.port.in.posttopic.ListTrendingPostTopicsUseCase;
import com.nongthinh.post_service.application.port.in.posttype.ListPostTypesUseCase;
import com.nongthinh.post_service.application.view.PostTopicView;
import com.nongthinh.post_service.application.view.PostTypeView;
import com.nongthinh.post_service.application.view.TrendingPostTopicView;
import com.nongthinh.post_service.common.response.ApiResponse;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequiredArgsConstructor
public class PostCatalogController {
    private final ListPostTypesUseCase listPostTypesUseCase;
    private final ListPostTopicsUseCase listPostTopicsUseCase;
    private final ListTrendingPostTopicsUseCase listTrendingPostTopicsUseCase;

    @GetMapping("post-types")
    public ResponseEntity<ApiResponse<List<PostTypeView>>> listPostTypes() {
        return ResponseEntity.ok(ApiResponse.<List<PostTypeView>>builder()
                .message("Post types retrieved successfully")
                .result(listPostTypesUseCase.execute(true))
                .build());
    }

    @GetMapping("post-topics")
    public ResponseEntity<ApiResponse<List<PostTopicView>>> listPostTopics() {
        return ResponseEntity.ok(ApiResponse.<List<PostTopicView>>builder()
                .message("Post topics retrieved successfully")
                .result(listPostTopicsUseCase.execute(true))
                .build());
    }

    @GetMapping("post-topics/trending")
    public ResponseEntity<ApiResponse<List<TrendingPostTopicView>>> listTrendingPostTopics(
            @RequestParam(defaultValue = "3") @Min(1) @Max(20) int limit) {
        return ResponseEntity.ok(ApiResponse.<List<TrendingPostTopicView>>builder()
                .message("Trending post topics retrieved successfully")
                .result(listTrendingPostTopicsUseCase.execute(limit))
                .build());
    }
}
