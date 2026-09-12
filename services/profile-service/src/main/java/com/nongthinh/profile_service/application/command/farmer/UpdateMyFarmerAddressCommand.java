package com.nongthinh.profile_service.application.command.farmer;

import java.util.UUID;

public record UpdateMyFarmerAddressCommand(
    String provinceId,
    UUID communeId,
    String addressDetail
) {

}
