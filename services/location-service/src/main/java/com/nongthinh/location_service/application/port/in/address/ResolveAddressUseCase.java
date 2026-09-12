package com.nongthinh.location_service.application.port.in.address;

import java.util.UUID;
import com.nongthinh.location_service.application.view.AddressView;

public interface ResolveAddressUseCase {

    AddressView execute(String provinceId, UUID communeId);
}
