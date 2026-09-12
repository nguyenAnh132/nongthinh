package com.nongthinh.agri_catalog_service.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.nongthinh.agri_catalog_service.application.port.in.disease.impl.ListBrandDiseaseReviewHistoriesUseCaseImpl;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.presentation.advice.GlobalExceptionHandler;

@WebMvcTest(DiseaseReviewHistoryController.class)
@Import({
        ListBrandDiseaseReviewHistoriesUseCaseImpl.class,
        GlobalExceptionHandler.class,
        DiseaseReviewHistoryControllerTest.MethodSecurityConfiguration.class
})
class DiseaseReviewHistoryControllerTest {

    private static final UUID HISTORY_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID DISEASE_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID BRAND_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID ACTOR_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID =
            UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT =
            Instant.parse("2026-07-26T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiseaseReviewHistoryRepository repository;

    @BeforeEach
    void setUp() {
        when(repository.search(any())).thenReturn(new PageView<>(
                List.of(new DiseaseReviewHistoryListItemView(
                        HISTORY_ID,
                        DISEASE_ID,
                        "Rice Blast",
                        CROP_TYPE_ID,
                        CreatedSource.BRAND,
                        BRAND_ID,
                        DiseaseReviewAction.APPROVED,
                        ReviewStatus.PENDING_REVIEW,
                        ReviewStatus.APPROVED,
                        "Approved",
                        ACTOR_ID,
                        ReviewActorType.ADMIN,
                        CREATED_AT
                )),
                0,
                5,
                1,
                1,
                false
        ));
    }

    @Test
    void adminReceivesCompleteStableJsonContract() throws Exception {
        mockMvc.perform(get("/disease-review-histories")
                        .param("createdSource", "BRAND")
                        .param("page", "0")
                        .param("size", "5")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.result.items[0].id").value(HISTORY_ID.toString()))
                .andExpect(jsonPath("$.result.items[0].diseaseId").value(DISEASE_ID.toString()))
                .andExpect(jsonPath("$.result.items[0].diseaseName").value("Rice Blast"))
                .andExpect(jsonPath("$.result.items[0].cropTypeId").value(CROP_TYPE_ID.toString()))
                .andExpect(jsonPath("$.result.items[0].createdSource").value("BRAND"))
                .andExpect(jsonPath("$.result.items[0].brandId").value(BRAND_ID.toString()))
                .andExpect(jsonPath("$.result.items[0].action").value("APPROVED"))
                .andExpect(jsonPath("$.result.items[0].previousStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.result.items[0].newStatus").value("APPROVED"))
                .andExpect(jsonPath("$.result.items[0].comment").value("Approved"))
                .andExpect(jsonPath("$.result.items[0].actorId").value(ACTOR_ID.toString()))
                .andExpect(jsonPath("$.result.items[0].actorType").value("ADMIN"))
                .andExpect(jsonPath("$.result.items[0].createdAt").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(5))
                .andExpect(jsonPath("$.result.totalElements").value(1))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.hasNext").value(false));
    }

    @Test
    void brandIsForbidden() throws Exception {
        mockMvc.perform(get("/disease-review-histories")
                        .with(user("brand").authorities(() -> "ROLE_BRAND")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidEnumsPaginationDateAndScopeReturnValidationError() throws Exception {
        assertBadRequest("action", "UNKNOWN");
        assertBadRequest("page", "-1");
        assertBadRequest("size", "101");
        assertBadRequest("from", "not-a-date");
        assertBadRequest("createdSource", "ADMIN");

        mockMvc.perform(get("/disease-review-histories")
                        .param("from", "2026-07-27T00:00:00Z")
                        .param("to", "2026-07-26T00:00:00Z")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_INVALID_REQUEST_PARAMETER"));
    }

    private void assertBadRequest(String name, String value) throws Exception {
        mockMvc.perform(get("/disease-review-histories")
                        .param(name, value)
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_INVALID_REQUEST_PARAMETER"));
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }
}
