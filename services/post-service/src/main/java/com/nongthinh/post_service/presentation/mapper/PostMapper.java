package com.nongthinh.post_service.presentation.mapper;

import com.nongthinh.post_service.application.command.CreatePostCommand;
import com.nongthinh.post_service.application.command.UpdatePostCommand;
import com.nongthinh.post_service.presentation.dto.request.CreatePostRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    CreatePostCommand toCommand(CreatePostRequest request);
    UpdatePostCommand toCommand(UpdatePostRequest request);
}
