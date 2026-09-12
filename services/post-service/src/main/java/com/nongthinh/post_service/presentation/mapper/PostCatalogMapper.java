package com.nongthinh.post_service.presentation.mapper;

import com.nongthinh.post_service.application.command.CreatePostTopicCommand;
import com.nongthinh.post_service.application.command.CreatePostTypeCommand;
import com.nongthinh.post_service.application.command.UpdatePostTopicCommand;
import com.nongthinh.post_service.application.command.UpdatePostTypeCommand;
import com.nongthinh.post_service.presentation.dto.request.CreatePostTopicRequest;
import com.nongthinh.post_service.presentation.dto.request.CreatePostTypeRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostTopicRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostTypeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostCatalogMapper {
    CreatePostTypeCommand toCommand(CreatePostTypeRequest request);
    UpdatePostTypeCommand toCommand(UpdatePostTypeRequest request);
    CreatePostTopicCommand toCommand(CreatePostTopicRequest request);
    UpdatePostTopicCommand toCommand(UpdatePostTopicRequest request);
}
