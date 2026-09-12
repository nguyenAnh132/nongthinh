package com.nongthinh.post_service.application.command;

public record UpdatePostTopicCommand(String name, String description, int displayOrder, boolean active) {
}
