package com.nongthinh.file_service.presentation.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.nongthinh.file_service.application.command.UploadFileCommand;
import com.nongthinh.file_service.application.port.in.file.DeleteFileUseCase;
import com.nongthinh.file_service.application.port.in.file.GetFileContentUseCase;
import com.nongthinh.file_service.application.port.in.file.GetFileByIdUseCase;
import com.nongthinh.file_service.application.port.in.file.ListMyFilesUseCase;
import com.nongthinh.file_service.application.port.in.file.UploadFileUseCase;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.application.view.FileView;
import com.nongthinh.file_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.common.response.ApiResponse;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final GetFileContentUseCase getFileContentUseCase;
    private final GetFileByIdUseCase getFileByIdUseCase;
    private final ListMyFilesUseCase listMyFilesUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final ObjectStoragePort objectStoragePort;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileView>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("purpose") String purpose) throws IOException {

        var currentUser = currentUserProvider.getCurrentUser();

        UploadFileCommand command = new UploadFileCommand(
                currentUser.getUserId(),
                purpose,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream());

        FileView view = uploadFileUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<FileView>builder()
                .message("File uploaded successfully")
                .result(Optional.of(view))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileView>> getFileById(@PathVariable UUID id) {
        FileView view = getFileByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<FileView>builder()
                .message("File retrieved successfully")
                .result(Optional.of(view))
                .build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> getFileContent(@PathVariable UUID id) {
        return streamFile(getFileContentUseCase.execute(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<FileView>>> listMyFiles(@RequestParam("purpose") String purpose) {
        List<FileView> views = listMyFilesUseCase.execute(purpose);
        return ResponseEntity.ok(ApiResponse.<List<FileView>>builder()
                .message("Files retrieved successfully")
                .result(Optional.of(views))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable UUID id) {
        deleteFileUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("File deleted successfully")
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
