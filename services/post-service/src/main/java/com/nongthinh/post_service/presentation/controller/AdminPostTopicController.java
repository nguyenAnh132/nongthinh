package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.posttopic.CreatePostTopicUseCase;
import com.nongthinh.post_service.application.port.in.posttopic.DeletePostTopicUseCase;
import com.nongthinh.post_service.application.port.in.posttopic.GetPostTopicUseCase;
import com.nongthinh.post_service.application.port.in.posttopic.ListPostTopicsUseCase;
import com.nongthinh.post_service.application.port.in.posttopic.UpdatePostTopicUseCase;
import com.nongthinh.post_service.application.view.PostTopicView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.presentation.dto.request.CreatePostTopicRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostTopicRequest;
import com.nongthinh.post_service.presentation.mapper.PostCatalogMapper;
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
@RequestMapping("admin/post-topics")
@PreAuthorize("hasAuthority('content:moderate')")
@RequiredArgsConstructor
public class AdminPostTopicController {
    private final ListPostTopicsUseCase listUseCase;
    private final GetPostTopicUseCase getUseCase;
    private final CreatePostTopicUseCase createUseCase;
    private final UpdatePostTopicUseCase updateUseCase;
    private final DeletePostTopicUseCase deleteUseCase;
    private final PostCatalogMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostTopicView>>> list() {
        return ResponseEntity.ok(ApiResponse.<List<PostTopicView>>builder()
                .message("Post topics retrieved successfully").result(listUseCase.execute(false)).build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<PostTopicView>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PostTopicView>builder()
                .message("Post topic retrieved successfully").result(getUseCase.execute(id)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostTopicView>> create(@RequestBody @Valid CreatePostTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PostTopicView>builder()
                .message("Post topic created successfully").result(createUseCase.execute(mapper.toCommand(request))).build());
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<PostTopicView>> update(@PathVariable UUID id,
                                                             @RequestBody @Valid UpdatePostTopicRequest request) {
        return ResponseEntity.ok(ApiResponse.<PostTopicView>builder()
                .message("Post topic updated successfully").result(updateUseCase.execute(id, mapper.toCommand(request))).build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Post topic deleted successfully").build());
    }
}
