package com.nongthinh.profile_service.application.port.in.follow;
import com.nongthinh.profile_service.application.event.PostPublishedEvent;
public interface HandleFollowedPostUseCase { void execute(PostPublishedEvent event); }
