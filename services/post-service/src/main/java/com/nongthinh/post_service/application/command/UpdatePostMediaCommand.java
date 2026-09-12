package com.nongthinh.post_service.application.command;

import java.util.UUID;

public record UpdatePostMediaCommand(UUID fileId, int displayOrder, String caption) {
}
