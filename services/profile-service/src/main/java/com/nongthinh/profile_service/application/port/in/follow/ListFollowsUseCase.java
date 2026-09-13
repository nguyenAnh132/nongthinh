package com.nongthinh.profile_service.application.port.in.follow;
import java.util.UUID;
import com.nongthinh.profile_service.application.view.FollowPageView;
public interface ListFollowsUseCase { FollowPageView execute(UUID userId, boolean followers, int page, int size); }
