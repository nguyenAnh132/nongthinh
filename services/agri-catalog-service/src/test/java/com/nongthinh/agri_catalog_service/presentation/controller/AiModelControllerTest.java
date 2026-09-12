package com.nongthinh.agri_catalog_service.presentation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.CreateAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.DeleteAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.GetAiModelByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.ListAiModelsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.UpdateAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.agri_catalog_service.presentation.mapper.AiModelMapper;

@WebMvcTest(AiModelController.class)
@Import({
        GlobalExceptionHandler.class,
        AiModelControllerTest.MethodSecurityConfiguration.class
})
class AiModelControllerTest {

    private static final UUID MODEL_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID CROP_TYPE_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ListAiModelsUseCase listAiModelsUseCase;
    @MockitoBean private GetAiModelByIdUseCase getAiModelByIdUseCase;
    @MockitoBean private CreateAiModelUseCase createAiModelUseCase;
    @MockitoBean private UpdateAiModelUseCase updateAiModelUseCase;
    @MockitoBean private DeleteAiModelUseCase deleteAiModelUseCase;
    @MockitoBean private AiModelMapper aiModelMapper;

    @Test
    void adminCanListAiModels() throws Exception {
        when(listAiModelsUseCase.execute()).thenReturn(List.of(aiModelView()));

        mockMvc.perform(get("/ai-models")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(MODEL_ID.toString()))
                .andExpect(jsonPath("$.result[0].taskType").value("DISEASE_DETECTION"))
                .andExpect(jsonPath("$.result[0].cropTypeIds[0]").value(CROP_TYPE_ID.toString()));

        verify(listAiModelsUseCase).execute();
    }

    @Test
    void brandCannotAccessAiModelRegistry() throws Exception {
        mockMvc.perform(get("/ai-models")
                        .with(user("brand").authorities(() -> "ROLE_BRAND")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(listAiModelsUseCase);
    }

    private AiModelView aiModelView() {
        return new AiModelView(
                MODEL_ID,
                "RICE_DETECTOR",
                "Rice detector",
                null,
                AiModelTaskType.DISEASE_DETECTION,
                CropCoverageType.SELECTED_CROPS,
                Set.of(CROP_TYPE_ID),
                AiModelStatus.DRAFT,
                NOW,
                ADMIN_ID,
                NOW,
                ADMIN_ID
        );
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }
}
