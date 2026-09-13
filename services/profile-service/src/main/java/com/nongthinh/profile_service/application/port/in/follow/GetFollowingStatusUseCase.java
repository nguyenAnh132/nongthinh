package com.nongthinh.profile_service.application.port.in.follow;
import java.util.*;
public interface GetFollowingStatusUseCase { Set<UUID> execute(List<UUID> userIds); }
