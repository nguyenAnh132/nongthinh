package com.nongthinh.post_service.presentation.mapper;

import com.nongthinh.post_service.application.command.CreatePostCommentCommand;
import com.nongthinh.post_service.application.command.UpdatePostCommentCommand;
import com.nongthinh.post_service.presentation.dto.request.CreatePostCommentRequest;
import com.nongthinh.post_service.presentation.dto.request.UpdatePostCommentRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostCommentMapper {
    CreatePostCommentCommand toCommand(CreatePostCommentRequest request);
    UpdatePostCommentCommand toCommand(UpdatePostCommentRequest request);
}
