package com.nongthinh.file_service.presentation.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.file_service.application.port.in.file.DeleteDiagnosisImageInternalUseCase;
import com.nongthinh.file_service.application.port.in.file.GetFileContentInternalUseCase;
import com.nongthinh.file_service.application.port.in.file.GetFileByIdInternalUseCase;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.application.view.FileView;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.common.response.ApiResponse;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("internal/files")
@PreAuthorize("hasAuthority('ROLE_INTERNAL')")
@RequiredArgsConstructor
public class InternalFileController {

    private final GetFileByIdInternalUseCase getFileByIdInternalUseCase;
    private final GetFileContentInternalUseCase getFileContentInternalUseCase;
    private final DeleteDiagnosisImageInternalUseCase deleteDiagnosisImageInternalUseCase;
    private final ObjectStoragePort objectStoragePort;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileView>> getFileById(@PathVariable UUID id) {
        FileView view = getFileByIdInternalUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<FileView>builder()
                .message("File retrieved successfully")
                .result(Optional.of(view))
                .build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> getFileContent(@PathVariable UUID id) {
        return streamFile(getFileContentInternalUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiagnosisImage(@PathVariable UUID id) {
        deleteDiagnosisImageInternalUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Diagnosis image deleted successfully")
                .build());
    }

    private ResponseEntity<InputStreamResource> streamFile(StoredFile file) {
        InputStream inputStream;
        try {
            inputStream = objectStoragePort.getObject(file.getBucket(), file.getObjectKey());
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE, ex);
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(file.getOriginalFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getSizeBytes())
                .body(new InputStreamResource(inputStream));
    }
}
