package com.nongthinh.profile_service.application.port.out;

import java.util.UUID;
import com.nongthinh.profile_service.application.view.AddressNamesView;

public interface LocationServicePort {

    void validateAddress(String provinceId, UUID communeId);

    AddressNamesView resolveAddress(String provinceId, UUID communeId);
}
