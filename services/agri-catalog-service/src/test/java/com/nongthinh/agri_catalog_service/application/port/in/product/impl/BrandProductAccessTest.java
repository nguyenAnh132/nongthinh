package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.nongthinh.agri_catalog_service.application.command.ProductUpdateCommand;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import com.nongthinh.agri_catalog_service.application.port.out.SnapshotSerializer;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductHistoryRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryActorType;

class BrandProductAccessTest {
    private static final UUID OWNER = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID PRODUCT = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID CATEGORY = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-08T00:00:00Z");
    private final ProductRepository products = mock(ProductRepository.class);
    private final ProductHistoryRepository histories = mock(ProductHistoryRepository.class);
    private final CurrentUserProvider users = mock(CurrentUserProvider.class);
    private final ProductCategoryRepository categories = mock(ProductCategoryRepository.class);
    private final ClockProvider clock = mock(ClockProvider.class);
    private final Product product = mock(Product.class);
    private final IdGenerator ids = mock(IdGenerator.class);
    private final ProductRatingQuery ratings = mock(ProductRatingQuery.class);
    private final ProductUseCaseSupport support = new ProductUseCaseSupport(products, histories, users,
            ids, mock(SnapshotSerializer.class));
    private final GetProductByIdUseCaseImpl get = new GetProductByIdUseCaseImpl(support, ratings);
    private final UpdateProductUseCaseImpl update = new UpdateProductUseCaseImpl(support, products, categories, clock, users);

    @BeforeEach
    void setUp() {
        when(product.getId()).thenReturn(PRODUCT);
        when(product.getBrandId()).thenReturn(OWNER);
        when(products.findById(PRODUCT)).thenReturn(Optional.of(product));
        when(ids.generate()).thenReturn(UUID.fromString("40000000-0000-0000-0000-000000000001"));
        when(ratings.summarizeVisibleReviews(any())).thenReturn(
                Map.of(PRODUCT, new ProductRatingSummary(PRODUCT, 4.5d, 12L))
        );
        signIn(OWNER, "ROLE_BRAND");
    }

    @Test
    void ownerCanReadOwnProduct() {
        assertEquals(PRODUCT, get.execute(PRODUCT).id());
        assertEquals(4.5d, get.execute(PRODUCT).averageRating());
        assertEquals(12L, get.execute(PRODUCT).reviewCount());
    }

    @Test
    void anotherBrandCannotReadProduct() {
        signIn(OTHER, "ROLE_BRAND");
        assertEquals(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED,
                assertThrows(BusinessException.class, () -> get.execute(PRODUCT)).getErrorCode());
    }

    @Test
    void adminCanReadProductAcrossBrands() {
        signIn(OTHER, "ROLE_ADMIN");
        assertEquals(PRODUCT, get.execute(PRODUCT).id());
    }

    @Test
    void missingProductKeepsNotFoundContract() {
        when(products.findById(PRODUCT)).thenReturn(Optional.empty());
        assertEquals(ErrorCode.PRODUCT_NOT_FOUND,
                assertThrows(BusinessException.class, () -> get.execute(PRODUCT)).getErrorCode());
    }

    @Test
    void ownerUpdateRecordsBrandActorAndKeepsOwner() {
        when(product.getSlug()).thenReturn("rice-product");
        when(categories.findById(CATEGORY)).thenReturn(Optional.of(mock(ProductCategory.class)));
        when(clock.now()).thenReturn(NOW);
        when(products.save(product)).thenReturn(product);
        assertEquals(OWNER, update.execute(PRODUCT, command()).brandId());
        verify(products).save(product);
        verify(histories).save(argThat(history -> history.getActorId().equals(OWNER)
                && history.getActorType() == ProductHistoryActorType.BRAND));
    }

    @Test
    void anotherBrandCannotUpdateOrRecordHistory() {
        signIn(OTHER, "ROLE_BRAND");
        assertEquals(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED,
                assertThrows(BusinessException.class, () -> update.execute(PRODUCT, command())).getErrorCode());
        verify(products, never()).save(any());
        verifyNoInteractions(histories, categories);
    }

    @Test
    void brandListIgnoresRequestedOtherBrand() {
        when(products.findAllByBrandId(OWNER)).thenReturn(List.of(product));
        var result = new ListProductsUseCaseImpl(products, users, ratings).execute(OTHER, null, false);
        assertEquals(List.of(PRODUCT), result.stream().map(item -> item.id()).toList());
        assertEquals(4.5d, result.getFirst().averageRating());
        assertEquals(12L, result.getFirst().reviewCount());
        verify(products).findAllByBrandId(OWNER);
        verify(products, never()).findAllByBrandId(OTHER);
        verify(products, never()).findAll();
    }

    private void signIn(UUID id, String role) {
        when(users.getCurrentUser()).thenReturn(new CurrentUser(id, null, null, Set.of(role), null, Set.of()));
    }

    private ProductUpdateCommand command() {
        return new ProductUpdateCommand(CATEGORY, "Rice product", "rice-product", null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
