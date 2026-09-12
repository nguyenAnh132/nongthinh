package com.nongthinh.post_service.domain.post;

import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.notBefore;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public final class PostType {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private final UUID id;
    private final String code;
    private String name;
    private String description;
    private int displayOrder;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private PostType(UUID id, String code, String name, String description, int displayOrder,
                     boolean active, Instant createdAt, Instant updatedAt) {
        this.id = requiredId(id, "postTypeId");
        this.code = validateCode(code);
        this.name = requiredText(name, 150, "name");
        this.description = optionalText(description, 500, "description");
        this.displayOrder = validateDisplayOrder(displayOrder);
        this.active = active;
        this.createdAt = required(createdAt, "createdAt");
        this.updatedAt = required(updatedAt, "updatedAt");
        notBefore(this.updatedAt, this.createdAt, "updatedAt", "createdAt");
    }

    public static PostType create(UUID id, String code, String name, String description,
                                  int displayOrder, Instant now) {
        return new PostType(id, code, name, description, displayOrder, true, now, now);
    }

    public static PostType reconstruct(UUID id, String code, String name, String description,
                                       int displayOrder, boolean active, Instant createdAt,
                                       Instant updatedAt) {
        return new PostType(id, code, name, description, displayOrder, active, createdAt, updatedAt);
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

    private static String validateCode(String code) {
        String value = requiredText(code, 50, "code");
        if (!CODE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("code must be uppercase snake case");
        }
        return value;
    }

    private static int validateDisplayOrder(int value) {
        if (value < 0) throw new IllegalArgumentException("displayOrder must not be negative");
        return value;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
