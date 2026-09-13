package com.nongthinh.profile_service.presentation.controller;
import com.nongthinh.profile_service.application.port.in.follow.*;
import com.nongthinh.profile_service.application.view.*;
import com.nongthinh.profile_service.common.response.ApiResponse;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/users")
@PreAuthorize("hasAnyAuthority('ROLE_FARMER', 'ROLE_BRAND')")
@RequiredArgsConstructor
public class FollowController {
    private final FollowUserUseCase follow;
    private final UnfollowUserUseCase unfollow;
    private final GetFollowProfileUseCase profile;
    private final ListFollowsUseCase list;
    private final GetFollowingStatusUseCase statuses;
    @GetMapping("/{userId}/follow-profile")
    public ApiResponse<FollowProfileView> profile(@PathVariable UUID userId) {
        return ApiResponse.<FollowProfileView>builder().result(profile.execute(userId)).build();
    }
    @PutMapping("/{userId}/follow")
    public ApiResponse<FollowProfileView> follow(@PathVariable UUID userId) {
        return ApiResponse.<FollowProfileView>builder().result(follow.execute(userId)).build();
    }
    @DeleteMapping("/{userId}/follow")
    public ApiResponse<Void> unfollow(@PathVariable UUID userId) {
        unfollow.execute(userId);
        return ApiResponse.<Void>builder().message("User unfollowed successfully").build();
    }
    @GetMapping("/{userId}/followers")
    public ApiResponse<FollowPageView> followers(@PathVariable UUID userId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ApiResponse.<FollowPageView>builder().result(list.execute(userId, true, page, size)).build();
    }
    @GetMapping("/{userId}/following")
    public ApiResponse<FollowPageView> following(@PathVariable UUID userId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ApiResponse.<FollowPageView>builder().result(list.execute(userId, false, page, size)).build();
    }
    @GetMapping("/following-status")
    public ApiResponse<Set<UUID>> statuses(@RequestParam List<UUID> userIds) {
        return ApiResponse.<Set<UUID>>builder().result(statuses.execute(userIds)).build();
    }
}
