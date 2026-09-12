package com.nongthinh.profile_service.application.command.brand;

import java.util.UUID;

public record UpdateMyBrandOfficeAddressCommand(
        String provinceId,
        UUID communeId,
        String addressDetail
) {
}
