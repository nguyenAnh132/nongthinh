package com.nongthinh.location_service.application.port.in.address;

import java.util.UUID;

public interface ValidateAddressUseCase {

    boolean execute(String provinceId, UUID communeId);
}
