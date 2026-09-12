package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.history.PostSnapshot;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PostSnapshotView(
        String content,
        UUID postTypeId,
        UUID postTopicId,
        Set<UUID> cropTypeIds,
        String locationText,
        List<MediaView> media,
        PostStatus status,
        PostVisibility visibility
) {
    public PostSnapshotView {
        cropTypeIds = Set.copyOf(cropTypeIds);
        media = List.copyOf(media);
    }

    public static PostSnapshotView from(PostSnapshot snapshot) {
        return new PostSnapshotView(
                snapshot.content(),
                snapshot.postTypeId(),
                snapshot.postTopicId(),
                snapshot.cropTypeIds(),
                snapshot.locationText(),
                snapshot.media().stream().map(MediaView::from).toList(),
                snapshot.status(),
                snapshot.visibility()
        );
    }

    public record MediaView(
            UUID fileId,
            String mediaUrl,
            String contentType,
            int displayOrder
    ) {
        private static MediaView from(PostSnapshot.MediaSnapshot media) {
            return new MediaView(
                    media.fileId(),
                    media.mediaUrl(),
                    media.contentType(),
                    media.displayOrder()
            );
        }
    }
}
