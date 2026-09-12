package com.nongthinh.post_service.application.port.in.post;
import com.nongthinh.post_service.application.view.PostEngagementView;
import java.util.UUID;
public interface GetPostEngagementUseCase { PostEngagementView execute(UUID postId); }
