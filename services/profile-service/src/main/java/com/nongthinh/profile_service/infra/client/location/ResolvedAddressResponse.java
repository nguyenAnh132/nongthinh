package com.nongthinh.profile_service.infra.client.location;

import java.util.UUID;

public record ResolvedAddressResponse(
        String provinceId,
        String provinceName,
        UUID communeId,
        String communeName
) {
}
