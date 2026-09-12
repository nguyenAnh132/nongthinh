package com.nongthinh.agri_catalog_service.infra.persistence.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductPublicationPersistenceTest {
    private static final UUID CATEGORY_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID = UUID.fromString("50000000-0000-0000-0000-000000000002");
    private static final UUID BRAND_ID = UUID.fromString("50000000-0000-0000-0000-000000000003");
    private static final Instant CREATED_AT = Instant.parse("2026-09-08T00:00:00Z");
    private static final Instant PUBLISHED_AT = Instant.parse("2026-09-08T01:00:00Z");

    @Autowired private ProductCategoryRepository categories;
    @Autowired private ProductRepository products;
    @Autowired private EntityManager entityManager;

    @Test
    void publishingExistingProductPersistsWithoutImmutableCategoryWarning() {
        categories.save(ProductCategory.create(
                CATEGORY_ID,
                null,
                "Fertilizer",
                "fertilizer-persistence-test",
                null,
                0,
                BRAND_ID,
                BRAND_ID,
                null,
                CREATED_AT
        ));
        products.save(Product.create(
                PRODUCT_ID,
                BRAND_ID,
                CATEGORY_ID,
                "Organic fertilizer",
                "organic-fertilizer-persistence-test",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PublicationStatus.DRAFT,
                ModerationStatus.NORMAL,
                null,
                BRAND_ID,
                CREATED_AT
        ));

        Logger hibernateLogger = (Logger) LoggerFactory.getLogger("org.hibernate.orm.core");
        ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        hibernateLogger.addAppender(appender);
        try {
            Product product = products.findById(PRODUCT_ID).orElseThrow();
            product.publish(BRAND_ID, PUBLISHED_AT);
            products.save(product);
            entityManager.flush();
            entityManager.clear();

            Product reloaded = products.findById(PRODUCT_ID).orElseThrow();
            assertEquals(PublicationStatus.PUBLISHED, reloaded.getPublicationStatus());
            assertEquals(CATEGORY_ID, reloaded.getCategoryId());
            assertFalse(appender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("HHH000502")));
        } finally {
            hibernateLogger.detachAppender(appender);
            appender.stop();
        }
    }
}
