package com.nongthinh.post_service.application.command;

public record CreatePostTopicCommand(String name, String slug, String description, int displayOrder) {
}
