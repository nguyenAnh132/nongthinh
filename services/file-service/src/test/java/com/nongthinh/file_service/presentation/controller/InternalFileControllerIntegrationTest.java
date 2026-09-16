package com.nongthinh.file_service.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InternalFileControllerIntegrationTest {

    private static final UUID FILE_ID = UUID.fromString("90000000-0000-0000-0000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("90000000-0000-0000-0000-000000000002");
    private static final UUID VIDEO_FILE_ID = UUID.fromString("90000000-0000-0000-0000-000000000003");
    private static final UUID AVATAR_FILE_ID = UUID.fromString("90000000-0000-0000-0000-000000000004");
    private static final String BUCKET = "nongthinh-files-test";
    private static final String OBJECT_KEY = "diagnosis-images/90000000-0000-0000-0000-000000000002/"
            + "90000000-0000-0000-0000-000000000001.png";
    private static final byte[] CONTENT = {1, 2, 3, 4};

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Autowired
    private ObjectStoragePort objectStoragePort;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        objectStoragePort.putObject(
                BUCKET,
                OBJECT_KEY,
                MediaType.IMAGE_PNG_VALUE,
                CONTENT.length,
                new ByteArrayInputStream(CONTENT)
        );
        storedFileRepository.save(StoredFile.create(
                FILE_ID,
                OWNER_ID,
                FilePurpose.DIAGNOSIS_IMAGE,
                StorageProvider.LOCAL,
                BUCKET,
                OBJECT_KEY,
                "field.png",
                MediaType.IMAGE_PNG_VALUE,
                CONTENT.length,
                null,
                Instant.parse("2026-08-11T00:00:00Z")
        ));
    }

    @Test
    void publicEndpointReturnsNotFoundForPrivateFile() throws Exception {
        HttpResponse<String> response = httpClient().send(
                HttpRequest.newBuilder(URI.create(url("/public/" + FILE_ID))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(404, response.statusCode());
    }

    @Test
    void publicEndpointStreamsPostVideoWithItsMediaType() throws Exception {
        String videoObjectKey = "post-videos/" + OWNER_ID + "/" + VIDEO_FILE_ID + ".mp4";
        byte[] videoContent = {5, 6, 7, 8};
        objectStoragePort.putObject(
                BUCKET,
                videoObjectKey,
                "video/mp4",
                videoContent.length,
                new ByteArrayInputStream(videoContent)
        );
        storedFileRepository.save(StoredFile.create(
                VIDEO_FILE_ID,
                OWNER_ID,
                FilePurpose.POST_VIDEO,
                StorageProvider.LOCAL,
                BUCKET,
                videoObjectKey,
                "harvest.mp4",
                "video/mp4",
                videoContent.length,
                "http://localhost/public/" + VIDEO_FILE_ID,
                Instant.parse("2026-08-11T00:00:00Z")
        ));

        HttpResponse<byte[]> response = httpClient().send(
                HttpRequest.newBuilder(URI.create(url("/public/" + VIDEO_FILE_ID))).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());
        assertEquals("video/mp4", response.headers().firstValue("content-type").orElseThrow());
        assertArrayEquals(videoContent, response.body());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "—Pngtree—farmer sprays pesticide agriculture concept_15113439.png",
            "Ảnh đại diện nông dân.png",
            "farmer \"avatar\".png",
            "avatar.png"
    })
    void publicAvatarPreservesEncodedFilenameAndStreamsContent(String originalFileName) throws Exception {
        String avatarObjectKey = "avatars/" + OWNER_ID + "/" + AVATAR_FILE_ID + ".png";
        objectStoragePort.putObject(BUCKET, avatarObjectKey, MediaType.IMAGE_PNG_VALUE,
                CONTENT.length, new ByteArrayInputStream(CONTENT));
        storedFileRepository.save(StoredFile.create(
                AVATAR_FILE_ID, OWNER_ID, FilePurpose.AVATAR, StorageProvider.LOCAL,
                BUCKET, avatarObjectKey, originalFileName, MediaType.IMAGE_PNG_VALUE, CONTENT.length,
                "http://localhost/public/" + AVATAR_FILE_ID, Instant.parse("2026-09-16T00:00:00Z")));

        try (HttpClient client = httpClient()) {
            HttpResponse<byte[]> response = client.send(
                    HttpRequest.newBuilder(URI.create(url("/public/" + AVATAR_FILE_ID))).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());

            assertEquals(200, response.statusCode());
            String header = response.headers().firstValue("content-disposition").orElseThrow();
            assertTrue(StandardCharsets.ISO_8859_1.newEncoder().canEncode(header));
            assertTrue(header.contains("filename*=UTF-8''"));
            ContentDisposition disposition = ContentDisposition.parse(header);
            assertEquals("inline", disposition.getType());
            assertEquals(originalFileName, disposition.getFilename());
            assertEquals(MediaType.IMAGE_PNG_VALUE, response.headers().firstValue("content-type").orElseThrow());
            assertEquals(Long.toString(CONTENT.length), response.headers().firstValue("content-length").orElseThrow());
            assertArrayEquals(CONTENT, response.body());
        }
    }

    @Test
    void internalContentRequiresApiKeyAndStreamsExpectedHeaders() throws Exception {
        HttpResponse<String> unauthorized = httpClient().send(
                HttpRequest.newBuilder(URI.create(url("/internal/files/" + FILE_ID + "/content"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(401, unauthorized.statusCode());

        HttpResponse<byte[]> response = httpClient().send(
                HttpRequest.newBuilder(URI.create(url("/internal/files/" + FILE_ID + "/content")))
                        .header("X-API-KEY", "file")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());
        assertEquals(MediaType.IMAGE_PNG_VALUE, response.headers().firstValue("content-type").orElseThrow());
        assertEquals(Long.toString(CONTENT.length), response.headers().firstValue("content-length").orElseThrow());
        assertArrayEquals(CONTENT, response.body());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
