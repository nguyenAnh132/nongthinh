package com.nongthinh.post_service.application.command;

public record UpdatePostTypeCommand(String name, String description, int displayOrder, boolean active) {
}
