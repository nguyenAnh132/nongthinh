package com.nongthinh.post_service.application.view;

import java.util.List;

public record PostFeedView(List<PostView> items, String nextCursor, boolean hasNext) { }
