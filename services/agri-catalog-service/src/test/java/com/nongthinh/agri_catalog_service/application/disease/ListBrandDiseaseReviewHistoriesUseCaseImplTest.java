package com.nongthinh.agri_catalog_service.application.disease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.nongthinh.agri_catalog_service.application.port.in.disease.impl.ListBrandDiseaseReviewHistoriesUseCaseImpl;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

class ListBrandDiseaseReviewHistoriesUseCaseImplTest {

    private final DiseaseReviewHistoryRepository repository =
            mock(DiseaseReviewHistoryRepository.class);
    private final ListBrandDiseaseReviewHistoriesUseCaseImpl useCase =
            new ListBrandDiseaseReviewHistoriesUseCaseImpl(repository);

    @Test
    void normalizesKeywordForcesBrandScopeAndPreservesPagination() {
        PageView<DiseaseReviewHistoryListItemView> expected =
                new PageView<>(List.of(), 2, 20, 45, 3, false);
        when(repository.search(any())).thenReturn(expected);

        PageView<DiseaseReviewHistoryListItemView> actual =
                useCase.execute(query(null, "  rice blast  ", null, null, 2, 20));

        ArgumentCaptor<DiseaseReviewHistoryQuery> captor =
                ArgumentCaptor.forClass(DiseaseReviewHistoryQuery.class);
        verify(repository).search(captor.capture());
        assertEquals(CreatedSource.BRAND, captor.getValue().createdSource());
        assertEquals("rice blast", captor.getValue().keyword());
        assertSame(expected, actual);
    }

    @Test
    void dropsBlankKeyword() {
        when(repository.search(any())).thenReturn(
                new PageView<>(List.of(), 0, 5, 0, 0, false)
        );

        useCase.execute(query(CreatedSource.BRAND, "  ", null, null, 0, 5));

        ArgumentCaptor<DiseaseReviewHistoryQuery> captor =
                ArgumentCaptor.forClass(DiseaseReviewHistoryQuery.class);
        verify(repository).search(captor.capture());
        assertNull(captor.getValue().keyword());
    }

    @Test
    void rejectsNonBrandScopeInvalidPageSizeAndDateRange() {
        assertInvalid(query(CreatedSource.ADMIN, null, null, null, 0, 20));
        assertInvalid(query(null, null, null, null, -1, 20));
        assertInvalid(query(null, null, null, null, 0, 0));
        assertInvalid(query(null, null, null, null, 0, 101));
        assertInvalid(query(
                null,
                null,
                Instant.parse("2026-07-27T00:00:00Z"),
                Instant.parse("2026-07-26T00:00:00Z"),
                0,
                20
        ));
    }

    private void assertInvalid(DiseaseReviewHistoryQuery query) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> useCase.execute(query)
        );
        assertEquals(ErrorCode.INVALID_REQUEST_PARAMETER, exception.getErrorCode());
    }

    private DiseaseReviewHistoryQuery query(
            CreatedSource source,
            String keyword,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        return new DiseaseReviewHistoryQuery(
                source,
                null,
                null,
                null,
                null,
                null,
                null,
                keyword,
                from,
                to,
                page,
                size
        );
    }
}
