package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.view.PostEngagementView;

import com.nongthinh.post_service.application.port.in.postreaction.GetPostReactionSummaryUseCase;
import com.nongthinh.post_service.application.port.in.postreaction.ListPostReactionsUseCase;
import com.nongthinh.post_service.application.view.PageView;
import org.springframework.web.bind.annotation.RequestParam;
import com.nongthinh.post_service.application.port.in.postreaction.RemovePostReactionUseCase;
import com.nongthinh.post_service.application.port.in.postreaction.SetPostReactionUseCase;
import com.nongthinh.post_service.application.view.PostReactionSummaryView;
import com.nongthinh.post_service.application.view.PostReactionView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.presentation.dto.request.SetPostReactionRequest;
import com.nongthinh.post_service.presentation.mapper.PostReactionMapper;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("{postId}/reactions")
@RequiredArgsConstructor
public class PostReactionController {
    private final GetPostReactionSummaryUseCase getPostReactionSummaryUseCase;
    private final ListPostReactionsUseCase listPostReactionsUseCase;
    private final SetPostReactionUseCase setPostReactionUseCase;
    private final RemovePostReactionUseCase removePostReactionUseCase;
    private final PostReactionMapper mapper;

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PageView<PostReactionView>>> listPostReactions(
            @PathVariable UUID postId,
            @RequestParam(required = false) ReactionType reactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageView<PostReactionView>>builder()
                .message("Post reaction users retrieved successfully")
                .result(listPostReactionsUseCase.execute(postId, reactionType, page, size)).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostReactionSummaryView>> getPostReactionSummary(
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(ApiResponse.<PostReactionSummaryView>builder()
                .message("Post reactions retrieved successfully")
                .result(getPostReactionSummaryUseCase.execute(postId)).build());
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostReactionView>> setPostReaction(
            @PathVariable UUID postId,
            @RequestBody @Valid SetPostReactionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<PostReactionView>builder()
                .message("Post reaction saved successfully")
                .result(setPostReactionUseCase.execute(postId, mapper.toCommand(request)))
                .build());
    }

    @DeleteMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
    public ResponseEntity<ApiResponse<PostEngagementView>> removePostReaction(@PathVariable UUID postId) {
        return ResponseEntity.ok(ApiResponse.<PostEngagementView>builder()
                .result(removePostReactionUseCase.execute(postId))
                .message("Post reaction removed successfully").build());
    }
}
