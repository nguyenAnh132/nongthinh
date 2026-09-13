package com.nongthinh.profile_service.application.view;
import java.util.List;
public record FollowPageView(List<FollowProfileView> items, int page, int size, boolean hasNext) {}
