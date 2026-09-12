package com.nongthinh.post_service.domain.post;

import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.notBefore;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public final class PostTopic {
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final UUID id;
    private String name;
    private final String slug;
    private String description;
    private int displayOrder;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private PostTopic(UUID id, String name, String slug, String description, int displayOrder,
                      boolean active, Instant createdAt, Instant updatedAt) {
        this.id = requiredId(id, "postTopicId");
        this.name = requiredText(name, 150, "name");
        this.slug = validateSlug(slug);
        this.description = optionalText(description, 500, "description");
        this.displayOrder = validateDisplayOrder(displayOrder);
        this.active = active;
        this.createdAt = required(createdAt, "createdAt");
        this.updatedAt = required(updatedAt, "updatedAt");
        notBefore(this.updatedAt, this.createdAt, "updatedAt", "createdAt");
    }

    public static PostTopic create(UUID id, String name, String slug, String description,
                                   int displayOrder, Instant now) {
        return new PostTopic(id, name, slug, description, displayOrder, true, now, now);
    }

    public static PostTopic reconstruct(UUID id, String name, String slug, String description,
                                        int displayOrder, boolean active, Instant createdAt,
                                        Instant updatedAt) {
        return new PostTopic(id, name, slug, description, displayOrder, active, createdAt, updatedAt);
    }

    public void update(String name, String description, int displayOrder, Instant now) {
        String validatedName = requiredText(name, 150, "name");
        String validatedDescription = optionalText(description, 500, "description");
        int validatedDisplayOrder = validateDisplayOrder(displayOrder);
        Instant changedAt = changeTime(now);
        this.name = validatedName;
        this.description = validatedDescription;
        this.displayOrder = validatedDisplayOrder;
        this.updatedAt = changedAt;
    }

    public void activate(Instant now) { Instant changedAt = changeTime(now); active = true; updatedAt = changedAt; }
    public void deactivate(Instant now) { Instant changedAt = changeTime(now); active = false; updatedAt = changedAt; }

    private Instant changeTime(Instant now) { return notBefore(now, updatedAt, "now", "updatedAt"); }

    private static String validateSlug(String slug) {
        String value = requiredText(slug, 180, "slug");
        if (!SLUG_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("slug must be lowercase kebab case");
        }
        return value;
    }

    private static int validateDisplayOrder(int value) {
        if (value < 0) throw new IllegalArgumentException("displayOrder must not be negative");
        return value;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
