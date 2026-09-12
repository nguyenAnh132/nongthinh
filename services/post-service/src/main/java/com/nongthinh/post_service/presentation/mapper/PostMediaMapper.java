package com.nongthinh.post_service.presentation.mapper;

import com.nongthinh.post_service.application.command.CreatePostMediaCommand;
import com.nongthinh.post_service.application.command.UpdatePostMediaCommand;
import com.nongthinh.post_service.presentation.dto.request.CreatePostMediaRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostMediaRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMediaMapper {
    CreatePostMediaCommand toCommand(CreatePostMediaRequest request);
    UpdatePostMediaCommand toCommand(UpdatePostMediaRequest request);
}
