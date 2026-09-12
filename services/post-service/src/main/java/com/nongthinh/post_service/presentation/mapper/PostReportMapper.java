package com.nongthinh.post_service.presentation.mapper;

import com.nongthinh.post_service.application.command.CompletePostReportCommand;
import com.nongthinh.post_service.application.command.CreatePostReportCommand;
import com.nongthinh.post_service.presentation.dto.request.CompletePostReportRequest;
import com.nongthinh.post_service.presentation.dto.request.CreatePostReportRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PostReportMapper {
    CreatePostReportCommand toCreatePostReportCommand(CreatePostReportRequest request);

    CompletePostReportCommand toCompletePostReportCommand(CompletePostReportRequest request);
}
