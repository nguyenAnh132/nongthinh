package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.application.model.CommentThread;
import java.util.List;

public record PostCommentThreadView(PostCommentView comment, List<PostCommentView> replies) {
    public PostCommentThreadView {
        replies = List.copyOf(replies);
    }

    public static PostCommentThreadView from(CommentThread thread) {
        return new PostCommentThreadView(
                PostCommentView.from(thread.comment()),
                thread.replies().stream().map(PostCommentView::from).toList()
        );
    }
}
