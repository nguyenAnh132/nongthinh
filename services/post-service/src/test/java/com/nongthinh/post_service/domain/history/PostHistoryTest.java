package com.nongthinh.post_service.domain.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostHistoryTest {
    @Test
    void userHistoryRequiresActorId() {
        PostSnapshot snapshot = new PostSnapshot("content", UUID.randomUUID(), null,
                Set.of(), null, List.of(), PostStatus.PUBLISHED, PostVisibility.PUBLIC);

        assertThatThrownBy(() -> PostHistory.create(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), null, HistoryActorType.USER, PostHistoryAction.CREATED,
                null, PostStatus.PUBLISHED, null, PostVisibility.PUBLIC, null, null, null,
                snapshot, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actorUserId");
    }

    @Test
    void snapshotNormalizesTextAndIsImmutable() {
        UUID cropTypeId = UUID.randomUUID();
        PostSnapshot snapshot = new PostSnapshot("  content  ", UUID.randomUUID(), null,
                Set.of(cropTypeId), "  Can Tho  ", List.of(),
                PostStatus.PUBLISHED, PostVisibility.PUBLIC);

        assertThat(snapshot.content()).isEqualTo("content");
        assertThat(snapshot.locationText()).isEqualTo("Can Tho");
        assertThatThrownBy(() -> snapshot.cropTypeIds().add(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void snapshotAllowsMissingPostType() {
        PostSnapshot snapshot = new PostSnapshot("content", null, null,
                Set.of(), null, List.of(), PostStatus.PUBLISHED, PostVisibility.PUBLIC);

        assertThat(snapshot.postTypeId()).isNull();
    }
}
