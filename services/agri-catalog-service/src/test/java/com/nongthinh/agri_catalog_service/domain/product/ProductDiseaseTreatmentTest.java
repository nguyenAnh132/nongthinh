package com.nongthinh.agri_catalog_service.domain.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.EffectivenessLevel;

class ProductDiseaseTreatmentTest {

    private static final Instant NOW = Instant.parse("2026-07-26T00:00:00Z");
    private static final UUID ACTOR_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );

    @Test
    void createsAndUpdatesTreatmentWithNonNegativePriority() {
        ProductDiseaseTreatment treatment = createTreatment(0);

        treatment.update(
                EffectivenessLevel.VERY_HIGH,
                2,
                "20 ml",
                "Spray",
                "Morning",
                "Every seven days",
                "Follow label",
                ACTOR_ID,
                NOW.plusSeconds(60)
        );

        assertEquals(EffectivenessLevel.VERY_HIGH, treatment.getEffectivenessLevel());
        assertEquals(2, treatment.getPriority());
        assertEquals(NOW.plusSeconds(60), treatment.getUpdatedAt());
    }

    @Test
    void negativePriorityIsRejectedByDomain() {
        assertThrows(IllegalArgumentException.class, () -> createTreatment(-1));

        ProductDiseaseTreatment treatment = createTreatment(0);
        assertThrows(
                IllegalArgumentException.class,
                () -> treatment.changePriority(-1, ACTOR_ID, NOW.plusSeconds(60))
        );
    }

    private ProductDiseaseTreatment createTreatment(int priority) {
        return ProductDiseaseTreatment.create(
                UUID.fromString("20000000-0000-0000-0000-000000000002"),
                UUID.fromString("30000000-0000-0000-0000-000000000003"),
                UUID.fromString("40000000-0000-0000-0000-000000000004"),
                UUID.fromString("50000000-0000-0000-0000-000000000005"),
                EffectivenessLevel.HIGH,
                priority,
                "10 ml",
                "Spray",
                "Morning",
                "Weekly",
                null,
                ACTOR_ID,
                NOW
        );
    }
}
