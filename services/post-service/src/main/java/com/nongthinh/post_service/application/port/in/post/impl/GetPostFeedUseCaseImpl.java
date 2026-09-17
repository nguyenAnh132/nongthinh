package com.nongthinh.post_service.application.port.in.post.impl;

import com.nongthinh.post_service.application.command.GetPostFeedCommand;
import com.nongthinh.post_service.application.model.PostFeedCursor;
import com.nongthinh.post_service.application.port.in.post.GetPostFeedUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.FollowingQuery;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.service.PostFeedCursorCodec;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostFeedView;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.domain.post.Post;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostFeedUseCaseImpl implements GetPostFeedUseCase {
    private static final int FEED_SIZE = 10;
    private final PostRepository repository;
    private final PostUseCaseSupport support;
    private final ClockProvider clock;
    private final CurrentUserProvider currentUser;
    private final FollowingQuery following;
    private final PostFeedCursorCodec cursors;

    @Override
    public PostFeedView execute(GetPostFeedCommand command) {
        String keyword = support.normalizeKeyword(command.keyword());
        UUID viewer = command.followingOnly() ? currentUser.getCurrentUserId() : null;
        // Bind continuation to the filters/viewer, without treating the cursor as authorization.
        String scope = UUID.nameUUIDFromBytes((command.postTypeId() + "|" + command.topicId() + "|"
                + command.cropTypeId() + "|" + command.authorUserId() + "|" + viewer + "|" + keyword)
                .getBytes(StandardCharsets.UTF_8)).toString();
        Instant now = clock.now();
        PostFeedCursor cursor = command.cursor() == null ? null : cursors.decode(command.cursor(), scope, now);
        Instant snapshotAt = cursor == null ? now : cursor.snapshotAt();
        Set<UUID> authors = command.followingOnly() ? following.findFollowingUserIds(viewer) : null;
        if (authors != null && authors.isEmpty()) return new PostFeedView(List.of(), null, false);

        List<Post> candidates = repository.findFeed(command.postTypeId(), command.topicId(),
                command.cropTypeId(), command.authorUserId(), keyword, authors,
                snapshotAt, cursor, FEED_SIZE + 1);
        boolean hasNext = candidates.size() > FEED_SIZE;
        List<Post> items = candidates.subList(0, Math.min(candidates.size(), FEED_SIZE));
        String nextCursor = null;
        if (hasNext) {
            Post last = items.getLast();
            nextCursor = cursors.encode(new PostFeedCursor(snapshotAt, last.getPublishedAt(),
                    last.getId().value(), scope));
        }
        return new PostFeedView(items.stream().map(PostView::from).toList(), nextCursor, hasNext);
    }
}
