package com.nongthinh.location_service.presentation.controller;

import java.util.Optional;
import java.util.UUID;
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
import com.nongthinh.location_service.application.port.in.commune.CreateCommuneUseCase;
import com.nongthinh.location_service.application.port.in.commune.DeleteCommuneUseCase;
import com.nongthinh.location_service.application.port.in.commune.GetCommuneByIdUseCase;
import com.nongthinh.location_service.application.port.in.commune.UpdateCommuneUseCase;
import com.nongthinh.location_service.application.view.CommuneView;
import com.nongthinh.location_service.common.response.ApiResponse;
import com.nongthinh.location_service.presentation.dto.request.CommuneCreationRequest;
import com.nongthinh.location_service.presentation.dto.request.CommuneUpdateRequest;
import com.nongthinh.location_service.presentation.mapper.CommuneMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("communes")
@RequiredArgsConstructor
public class CommuneController {

    private final GetCommuneByIdUseCase getCommuneByIdUseCase;
    private final CreateCommuneUseCase createCommuneUseCase;
    private final UpdateCommuneUseCase updateCommuneUseCase;
    private final DeleteCommuneUseCase deleteCommuneUseCase;
    private final CommuneMapper communeMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommuneView>> getCommuneById(@PathVariable UUID id) {
        CommuneView view = getCommuneByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<CommuneView>builder()
                .message("Commune retrieved successfully")
                .result(Optional.of(view))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<CommuneView>> createCommune(
            @RequestBody @Valid CommuneCreationRequest request
    ) {
        CommuneView view = createCommuneUseCase.execute(communeMapper.toCommuneCreationCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<CommuneView>builder()
                .message("Commune created successfully")
                .result(Optional.of(view))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<CommuneView>> updateCommune(
            @PathVariable UUID id,
            @RequestBody @Valid CommuneUpdateRequest request
    ) {
        CommuneView view = updateCommuneUseCase.execute(id, communeMapper.toCommuneUpdateCommand(request));
        return ResponseEntity.ok(ApiResponse.<CommuneView>builder()
                .message("Commune updated successfully")
                .result(Optional.of(view))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:write')")
    public ResponseEntity<ApiResponse<Void>> deleteCommune(@PathVariable UUID id) {
        deleteCommuneUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Commune deleted successfully")
                .build());
    }
}
