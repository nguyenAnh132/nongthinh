package com.nongthinh.agri_catalog_service.domain.croptype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CropTypeTest {

    private static final UUID CROP_TYPE_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );
    private static final UUID ACTOR_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000001"
    );
    private static final Instant CREATED_AT = Instant.parse("2026-08-09T00:00:00Z");

    @Test
    void normalizesCodeAndKeepsItStableWhenUpdated() {
        CropType cropType = CropType.create(
                CROP_TYPE_ID,
                " rice ",
                "Lúa",
                "Cây lúa",
                ACTOR_ID,
                CREATED_AT
        );

        cropType.update(
                "Lúa nước",
                "Cây lúa trồng ở ruộng nước",
                false,
                ACTOR_ID,
                CREATED_AT.plusSeconds(60)
        );

        assertEquals("RICE", cropType.getCode());
        assertEquals("Lúa nước", cropType.getName());
        assertFalse(cropType.isActive());
    }
}
