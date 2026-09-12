package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.SnapshotSerializer;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductHistoryRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;

class UpdateProductPublicationStatusUseCaseImplTest {
    private static final UUID OWNER = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID PRODUCT_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-08T00:00:00Z");
    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductHistoryRepository histories = mock(ProductHistoryRepository.class);
    private final CurrentUserProvider users = mock(CurrentUserProvider.class);
    private final ClockProvider clock = mock(ClockProvider.class);
    private final IdGenerator ids = mock(IdGenerator.class);
    private final Product product = mock(Product.class);
    private final ProductUseCaseSupport support = new ProductUseCaseSupport(
            products,
            histories,
            users,
            ids,
            mock(SnapshotSerializer.class)
    );
    private final UpdateProductPublicationStatusUseCaseImpl useCase =
            new UpdateProductPublicationStatusUseCaseImpl(support, products, clock, users);

    @BeforeEach
    void setUp() {
        when(product.getId()).thenReturn(PRODUCT_ID);
        when(product.getBrandId()).thenReturn(OWNER);
        when(product.getModerationStatus()).thenReturn(ModerationStatus.NORMAL);
        when(product.getPublicationStatus()).thenReturn(PublicationStatus.DRAFT, PublicationStatus.PUBLISHED);
        when(products.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(products.save(product)).thenReturn(product);
        when(clock.now()).thenReturn(NOW);
        when(ids.generate()).thenReturn(UUID.fromString("40000000-0000-0000-0000-000000000001"));
        signIn(OWNER);
    }

    @Test
    void ownerCanPublishAndHistoryIsRecorded() {
        useCase.execute(PRODUCT_ID, PublicationStatus.PUBLISHED);

        verify(product).publish(OWNER, NOW);
        verify(products).save(product);
        verify(histories).save(argThat(history -> history.getAction() == ProductHistoryAction.PUBLISHED
                && history.getActorId().equals(OWNER)
                && history.getPreviousPublicationStatus() == PublicationStatus.DRAFT));
    }

    @Test
    void anotherBrandCannotChangePublicationStatus() {
        signIn(OTHER);
        assertEquals(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED,
                assertThrows(BusinessException.class,
                        () -> useCase.execute(PRODUCT_ID, PublicationStatus.PUBLISHED)).getErrorCode());
        verify(products, never()).save(product);
    }

    @Test
    void lockedProductCannotChangePublicationStatus() {
        when(product.getModerationStatus()).thenReturn(ModerationStatus.LOCKED);
        assertEquals(ErrorCode.PRODUCT_LOCKED,
                assertThrows(BusinessException.class,
                        () -> useCase.execute(PRODUCT_ID, PublicationStatus.UNPUBLISHED)).getErrorCode());
        verify(products, never()).save(product);
    }

    @Test
    void draftCannotBeRequestedAsPublicationTransition() {
        assertEquals(ErrorCode.PRODUCT_PUBLICATION_STATUS_INVALID,
                assertThrows(BusinessException.class,
                        () -> useCase.execute(PRODUCT_ID, PublicationStatus.DRAFT)).getErrorCode());
        verify(products, never()).save(product);
    }

    private void signIn(UUID id) {
        when(users.getCurrentUser()).thenReturn(
                new CurrentUser(id, null, null, Set.of("ROLE_BRAND"), null, Set.of()));
    }
}
