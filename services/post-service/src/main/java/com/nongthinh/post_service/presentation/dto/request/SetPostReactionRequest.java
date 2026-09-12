package com.nongthinh.post_service.presentation.dto.request;

import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import jakarta.validation.constraints.NotNull;

public record SetPostReactionRequest(
        @NotNull(message = "REACTION_TYPE_REQUIRED") ReactionType reactionType
) {
}
