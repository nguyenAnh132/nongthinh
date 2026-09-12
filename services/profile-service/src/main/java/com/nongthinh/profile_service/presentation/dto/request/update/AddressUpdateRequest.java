package com.nongthinh.profile_service.presentation.dto.request.update;

import java.util.UUID;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(

    @Size(max = 10, message = "PROVINCE_ID_INVALID")
    String provinceId,

    UUID communeId,

    @Size(max = 500, message = "ADDRESS_DETAIL_TOO_LONG")
    String addressDetail
) {
}
