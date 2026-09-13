package com.nongthinh.profile_service.application.port.in.follow;
import java.util.UUID;
import com.nongthinh.profile_service.application.view.FollowProfileView;
public interface FollowUserUseCase { FollowProfileView execute(UUID userId); }
