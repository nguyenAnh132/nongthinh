package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.model.PostFeedCursor;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.DateTimeException;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PostFeedCursorCodec {
    public String encode(PostFeedCursor cursor) {
        String payload = String.join("|", "1", cursor.snapshotAt().toString(),
                cursor.publishedAt().toString(), cursor.postId().toString(), cursor.scope());
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public PostFeedCursor decode(String value, String scope, Instant now) {
        try {
            if (value == null || value.isBlank() || value.length() > 512) {
                throw new IllegalArgumentException("Invalid cursor length");
            }
            String[] fields = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)
                    .split("\\|", -1);
            if (fields.length != 5 || !"1".equals(fields[0]) || !scope.equals(fields[4])) {
                throw new IllegalArgumentException("Invalid cursor scope or version");
            }
            PostFeedCursor cursor = new PostFeedCursor(Instant.parse(fields[1]), Instant.parse(fields[2]),
                    UUID.fromString(fields[3]), fields[4]);
            if (cursor.snapshotAt().isAfter(now) || cursor.publishedAt().isAfter(cursor.snapshotAt())) {
                throw new IllegalArgumentException("Invalid cursor timestamps");
            }
            return cursor;
        } catch (IllegalArgumentException | DateTimeException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER, ex);
        }
    }
}
