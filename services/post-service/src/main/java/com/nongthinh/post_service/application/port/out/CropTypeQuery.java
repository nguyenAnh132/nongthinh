package com.nongthinh.post_service.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface CropTypeQuery {
    Set<UUID> findActiveIds(Set<UUID> cropTypeIds);
}
