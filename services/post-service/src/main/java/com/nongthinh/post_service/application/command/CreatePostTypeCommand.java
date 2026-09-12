package com.nongthinh.post_service.application.command;

public record CreatePostTypeCommand(String code, String name, String description, int displayOrder) {
}
