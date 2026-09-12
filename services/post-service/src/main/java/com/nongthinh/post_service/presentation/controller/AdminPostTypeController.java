package com.nongthinh.post_service.presentation.controller;

import com.nongthinh.post_service.application.port.in.posttype.CreatePostTypeUseCase;
import com.nongthinh.post_service.application.port.in.posttype.DeletePostTypeUseCase;
import com.nongthinh.post_service.application.port.in.posttype.GetPostTypeUseCase;
import com.nongthinh.post_service.application.port.in.posttype.ListPostTypesUseCase;
import com.nongthinh.post_service.application.port.in.posttype.UpdatePostTypeUseCase;
import com.nongthinh.post_service.application.view.PostTypeView;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.presentation.dto.request.CreatePostTypeRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostTypeRequest;
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
@RequestMapping("admin/post-types")
@PreAuthorize("hasAuthority('content:moderate')")
@RequiredArgsConstructor
public class AdminPostTypeController {
    private final ListPostTypesUseCase listUseCase;
    private final GetPostTypeUseCase getUseCase;
    private final CreatePostTypeUseCase createUseCase;
    private final UpdatePostTypeUseCase updateUseCase;
    private final DeletePostTypeUseCase deleteUseCase;
    private final PostCatalogMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostTypeView>>> list() {
        return ResponseEntity.ok(ApiResponse.<List<PostTypeView>>builder()
                .message("Post types retrieved successfully").result(listUseCase.execute(false)).build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<PostTypeView>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PostTypeView>builder()
                .message("Post type retrieved successfully").result(getUseCase.execute(id)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostTypeView>> create(@RequestBody @Valid CreatePostTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PostTypeView>builder()
                .message("Post type created successfully").result(createUseCase.execute(mapper.toCommand(request))).build());
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<PostTypeView>> update(@PathVariable UUID id,
                                                            @RequestBody @Valid UpdatePostTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.<PostTypeView>builder()
                .message("Post type updated successfully").result(updateUseCase.execute(id, mapper.toCommand(request))).build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Post type deleted successfully").build());
    }
}
