package com.nongthinh.post_service.domain.comment;

import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.notBefore;

import com.nongthinh.post_service.domain.comment.valueobject.CommentStatus;
import java.time.Instant;
import java.util.UUID;

public final class Comment {
    public static final int CONTENT_MAX_LENGTH = 2_000;

    private final UUID id;
    private final UUID postId;
    private final UUID parentCommentId;
    private final UUID authorUserId;
    private String content;
    private CommentStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Comment(UUID id, UUID postId, UUID parentCommentId, UUID authorUserId,
                    String content, CommentStatus status, Instant createdAt,
                    Instant updatedAt, Instant deletedAt) {
        this.id = requiredId(id, "commentId");
        this.postId = requiredId(postId, "postId");
        this.parentCommentId = parentCommentId;
        this.authorUserId = requiredId(authorUserId, "authorUserId");
        this.content = requiredText(content, CONTENT_MAX_LENGTH, "content");
        this.status = required(status, "status");
        this.createdAt = required(createdAt, "createdAt");
        this.updatedAt = required(updatedAt, "updatedAt");
        this.deletedAt = deletedAt;
        if (this.id.equals(parentCommentId)) {
            throw new IllegalArgumentException("A comment cannot reply to itself");
        }
        notBefore(this.updatedAt, this.createdAt, "updatedAt", "createdAt");
        if (deletedAt != null) notBefore(this.updatedAt, deletedAt, "updatedAt", "deletedAt");
    }

    public static Comment createRoot(UUID id, UUID postId, UUID authorUserId,
                                     String content, Instant now) {
        return new Comment(id, postId, null, authorUserId, content,
                CommentStatus.PUBLISHED, now, now, null);
    }

    public static Comment createReply(UUID id, Comment parent, UUID authorUserId,
                                      String content, Instant now) {
        required(parent, "parent");
        if (parent.parentCommentId != null) {
            throw new IllegalArgumentException("Replies are limited to one level");
        }
        parent.assertNotDeleted();
        if (parent.status != CommentStatus.PUBLISHED) {
            throw new IllegalStateException("A hidden comment cannot receive replies");
        }
        return new Comment(id, parent.postId, parent.id, authorUserId, content,
                CommentStatus.PUBLISHED, now, now, null);
    }

    public static Comment reconstruct(UUID id, UUID postId, UUID parentCommentId, UUID authorUserId,
                                      String content, CommentStatus status, Instant createdAt,
                                      Instant updatedAt, Instant deletedAt) {
        return new Comment(id, postId, parentCommentId, authorUserId, content, status,
                createdAt, updatedAt, deletedAt);
    }

    public void edit(UUID actorUserId, String content, Instant now) {
        assertAuthor(actorUserId);
        assertNotDeleted();
        if (status == CommentStatus.HIDDEN) {
            throw new IllegalStateException("A hidden comment cannot be edited by its author");
        }
        String validatedContent = requiredText(content, CONTENT_MAX_LENGTH, "content");
        Instant changedAt = changeTime(now);
        this.content = validatedContent;
        this.updatedAt = changedAt;
    }

    public void hide(Instant now) {
        assertNotDeleted();
        if (status != CommentStatus.PUBLISHED) {
            throw new IllegalStateException("Only a published comment can be hidden");
        }
        Instant changedAt = changeTime(now);
        status = CommentStatus.HIDDEN;
        updatedAt = changedAt;
    }

    public void restore(Instant now) {
        assertNotDeleted();
        if (status != CommentStatus.HIDDEN) {
            throw new IllegalStateException("Only a hidden comment can be restored");
        }
        Instant changedAt = changeTime(now);
        status = CommentStatus.PUBLISHED;
        updatedAt = changedAt;
    }

    public void softDelete(UUID actorUserId, Instant now) {
        assertAuthor(actorUserId);
        assertNotDeleted();
        Instant changedAt = changeTime(now);
        deletedAt = changedAt;
        updatedAt = changedAt;
    }

    private void assertAuthor(UUID actor) {
        if (!authorUserId.equals(requiredId(actor, "actorUserId"))) throw new IllegalArgumentException("Only the comment author may perform this action");
    }
    private void assertNotDeleted() { if (deletedAt != null) throw new IllegalStateException("A deleted comment cannot be changed"); }
    private Instant changeTime(Instant now) { return notBefore(now, updatedAt, "now", "updatedAt"); }

    public UUID getId() { return id; }
    public UUID getPostId() { return postId; }
    public UUID getParentCommentId() { return parentCommentId; }
    public UUID getAuthorUserId() { return authorUserId; }
    public String getContent() { return content; }
    public CommentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public boolean isDeleted() { return deletedAt != null; }
}
