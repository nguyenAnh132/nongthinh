package com.nongthinh.agri_catalog_service.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductsUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductHistoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.GetProductByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.CreateProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.UpdateProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.UpdateProductPublicationStatusUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.product.DeleteProductUseCase;
import com.nongthinh.agri_catalog_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.agri_catalog_service.presentation.mapper.ProductMapper;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class, BrandProductControllerTest.MethodSecurityConfiguration.class})
class BrandProductControllerTest {
    private static final UUID ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final String PAYLOAD = """
            {"categoryId":"30000000-0000-0000-0000-000000000001","name":"Rice product","slug":"rice-product"}
            """;
    @Autowired private MockMvc mvc;
    @MockitoBean private ListProductsUseCase list;
    @MockitoBean private ListProductHistoryUseCase history;
    @MockitoBean private GetProductByIdUseCase get;
    @MockitoBean private CreateProductUseCase create;
    @MockitoBean private UpdateProductUseCase update;
    @MockitoBean private UpdateProductPublicationStatusUseCase updatePublicationStatus;
    @MockitoBean private DeleteProductUseCase delete;
    @MockitoBean private ProductMapper mapper;

    @Test
    void brandCanReadAndUpdate() throws Exception {
        mvc.perform(get("/products/{id}", ID).with(user("brand").roles("BRAND"))).andExpect(status().isOk());
        mvc.perform(put("/products/{id}", ID).with(csrf()).with(user("brand").roles("BRAND"))
                .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD)).andExpect(status().isOk());
        verify(get).execute(ID);
        verify(update).execute(eq(ID), any());
    }

    @Test
    void farmerCannotReadOrUpdate() throws Exception {
        mvc.perform(get("/products/{id}", ID).with(user("farmer").roles("FARMER"))).andExpect(status().isForbidden());
        mvc.perform(put("/products/{id}", ID).with(csrf()).with(user("farmer").roles("FARMER"))
                .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD)).andExpect(status().isForbidden());
        verifyNoInteractions(get, update);
    }

    @Test
    void brandStillCannotDelete() throws Exception {
        mvc.perform(delete("/products/{id}", ID).with(csrf()).with(user("brand").roles("BRAND"))).andExpect(status().isForbidden());
        verifyNoInteractions(delete);
    }

    @Test
    void brandCanPublishAndUnpublishOwnProduct() throws Exception {
        mvc.perform(post("/products/{id}/publish", ID).with(csrf()).with(user("brand").roles("BRAND")))
                .andExpect(status().isOk());
        mvc.perform(post("/products/{id}/unpublish", ID).with(csrf()).with(user("brand").roles("BRAND")))
                .andExpect(status().isOk());

        verify(updatePublicationStatus).execute(ID,
                com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus.PUBLISHED);
        verify(updatePublicationStatus).execute(ID,
                com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus.UNPUBLISHED);
    }

    @Test
    void farmerCannotChangePublicationStatus() throws Exception {
        mvc.perform(post("/products/{id}/publish", ID).with(csrf()).with(user("farmer").roles("FARMER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(updatePublicationStatus);
    }

    @Test
    void updateRejectsBlankName() throws Exception {
        mvc.perform(put("/products/{id}", ID).with(csrf()).with(user("brand").roles("BRAND"))
                .contentType(MediaType.APPLICATION_JSON).content(PAYLOAD.replace("Rice product", " ")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(update);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration { }
}
