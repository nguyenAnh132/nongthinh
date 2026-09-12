package com.nongthinh.agri_catalog_service.presentation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ApproveDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.CreateDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.DeleteDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.GetDiseaseByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.HideDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseaseReviewHistoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseasesUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.RejectDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.RestoreDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.SubmitDiseaseForReviewUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.disease.UpdateDiseaseUseCase;
import com.nongthinh.agri_catalog_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.agri_catalog_service.presentation.mapper.DiseaseMapper;

@WebMvcTest(DiseaseController.class)
@Import({
        GlobalExceptionHandler.class,
        DiseaseControllerRestoreTest.MethodSecurityConfiguration.class
})
class DiseaseControllerRestoreTest {

    private static final UUID DISEASE_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListDiseasesUseCase listDiseasesUseCase;
    @MockitoBean
    private GetDiseaseByIdUseCase getDiseaseByIdUseCase;
    @MockitoBean
    private CreateDiseaseUseCase createDiseaseUseCase;
    @MockitoBean
    private UpdateDiseaseUseCase updateDiseaseUseCase;
    @MockitoBean
    private DeleteDiseaseUseCase deleteDiseaseUseCase;
    @MockitoBean
    private SubmitDiseaseForReviewUseCase submitDiseaseForReviewUseCase;
    @MockitoBean
    private ApproveDiseaseUseCase approveDiseaseUseCase;
    @MockitoBean
    private RejectDiseaseUseCase rejectDiseaseUseCase;
    @MockitoBean
    private HideDiseaseUseCase hideDiseaseUseCase;
    @MockitoBean
    private RestoreDiseaseUseCase restoreDiseaseUseCase;
    @MockitoBean
    private ListDiseaseReviewHistoryUseCase listDiseaseReviewHistoryUseCase;
    @MockitoBean
    private DiseaseMapper diseaseMapper;

    @Test
    void adminCanRestoreHiddenDisease() throws Exception {
        when(restoreDiseaseUseCase.execute(DISEASE_ID)).thenReturn(null);

        mockMvc.perform(post("/diseases/{id}/restore", DISEASE_ID)
                        .with(csrf())
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Disease restored successfully"));

        verify(restoreDiseaseUseCase).execute(DISEASE_ID);
    }

    @Test
    void brandCannotRestoreHiddenDisease() throws Exception {
        mockMvc.perform(post("/diseases/{id}/restore", DISEASE_ID)
                        .with(csrf())
                        .with(user("brand").authorities(() -> "ROLE_BRAND")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(restoreDiseaseUseCase);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }
}
