package com.nongthinh.bo_portal_service.application.view;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;

public record SystemParamValueView(
        String name,
        String value,
        String dataType
) {
    public static SystemParamValueView fromSystemPram (SystemParam systemParam) {
        return new SystemParamValueView(
                systemParam.getName(),
                systemParam.getValue(),
                systemParam.getDataType().toString()
        );
    }
}