package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.posthistory.GetPostHistoryUseCase;
import com.nongthinh.post_service.application.port.in.posthistory.ListPostHistoriesUseCase;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostHistoryView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/post-histories")
@PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('content:moderate')")
@Validated
@RequiredArgsConstructor
public class AdminPostHistoryController {
    private final ListPostHistoriesUseCase listPostHistoriesUseCase;
    private final GetPostHistoryUseCase getPostHistoryUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageView<PostHistoryView>>> listPostHistories(
            @RequestParam(required = false) UUID postId,
            @RequestParam(required = false) UUID postAuthorUserId,
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) HistoryActorType actorType,
            @RequestParam(required = false) PostHistoryAction action,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageView<PostHistoryView>>builder()
                .message("Post histories retrieved successfully")
                .result(listPostHistoriesUseCase.execute(
                        postId,
                        postAuthorUserId,
                        actorUserId,
                        actorType,
                        action,
                        page,
                        size
                ))
                .build());
    }

    @GetMapping("{historyId}")
    public ResponseEntity<ApiResponse<PostHistoryView>> getPostHistory(
            @PathVariable UUID historyId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostHistoryView>builder()
                .message("Post history retrieved successfully")
                .result(getPostHistoryUseCase.execute(historyId))
                .build());
    }
}
