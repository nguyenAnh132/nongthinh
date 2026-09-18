package com.nongthinh.file_service.presentation.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.file_service.application.port.in.file.GetActiveFileForPublicAccessUseCase;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("public")
@RequiredArgsConstructor
public class PublicFileController {

    private final GetActiveFileForPublicAccessUseCase getActiveFileForPublicAccessUseCase;
    private final ObjectStoragePort objectStoragePort;

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> serveFile(@PathVariable UUID id) {
        StoredFile file = getActiveFileForPublicAccessUseCase.execute(id);
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
