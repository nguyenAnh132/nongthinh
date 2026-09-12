package com.nongthinh.post_service.application.command;

import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;

public record SetPostReactionCommand(ReactionType reactionType) {
}
