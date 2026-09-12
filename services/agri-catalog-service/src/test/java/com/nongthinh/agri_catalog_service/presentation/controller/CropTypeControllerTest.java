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
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.CreateCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.DeleteCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.GetCropTypeByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.ListCropTypesUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.UpdateCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import com.nongthinh.agri_catalog_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.agri_catalog_service.presentation.mapper.CropTypeMapper;

@WebMvcTest(CropTypeController.class)
@Import({
        GlobalExceptionHandler.class,
        CropTypeControllerTest.MethodSecurityConfiguration.class
})
class CropTypeControllerTest {

    private static final UUID CROP_TYPE_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000001"
    );
    private static final UUID ADMIN_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000001"
    );
    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListCropTypesUseCase listCropTypesUseCase;
    @MockitoBean
    private GetCropTypeByIdUseCase getCropTypeByIdUseCase;
    @MockitoBean
    private CreateCropTypeUseCase createCropTypeUseCase;
    @MockitoBean
    private UpdateCropTypeUseCase updateCropTypeUseCase;
    @MockitoBean
    private DeleteCropTypeUseCase deleteCropTypeUseCase;
    @MockitoBean
    private CropTypeMapper cropTypeMapper;

    @Test
    void adminCanListCropTypes() throws Exception {
        when(listCropTypesUseCase.execute(false)).thenReturn(List.of(new CropTypeView(
                CROP_TYPE_ID,
                "RICE",
                "Lúa",
                null,
                true,
                NOW,
                ADMIN_ID,
                NOW,
                ADMIN_ID
        )));

        mockMvc.perform(get("/crop-types")
                        .with(user("admin").authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(CROP_TYPE_ID.toString()))
                .andExpect(jsonPath("$.result[0].code").value("RICE"))
                .andExpect(jsonPath("$.result[0].active").value(true));

        verify(listCropTypesUseCase).execute(false);
    }

    @Test
    void brandCannotListCropTypes() throws Exception {
        mockMvc.perform(get("/crop-types")
                        .with(user("brand").authorities(() -> "ROLE_BRAND")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(listCropTypesUseCase);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }
}
