package com.nongthinh.post_service.presentation.mapper;

import com.nongthinh.post_service.application.command.SetPostReactionCommand;
import com.nongthinh.post_service.presentation.dto.request.SetPostReactionRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostReactionMapper {
    SetPostReactionCommand toCommand(SetPostReactionRequest request);
}
