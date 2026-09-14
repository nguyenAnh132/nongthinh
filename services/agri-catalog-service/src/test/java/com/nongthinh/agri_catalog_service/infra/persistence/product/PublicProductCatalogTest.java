package com.nongthinh.agri_catalog_service.infra.persistence.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.product.SearchPublicProductsUseCase;
import com.nongthinh.agri_catalog_service.application.query.PublicProductQuery;
import com.nongthinh.agri_catalog_service.infra.persistence.disease.JpaDiseaseEntity;
import com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment.JpaProductDiseaseTreatmentEntity;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PublicProductCatalogTest {
    private static final Instant NOW = Instant.parse("2026-09-13T00:00:00Z");
    @Autowired private EntityManager entityManager;
    @Autowired private SearchPublicProductsUseCase search;
    @Autowired private MockMvc mvc;

    @Test
    void searchesAcrossBrandsAndCombinesNameAndDiseaseWithoutDuplicatingProducts() {
        product(1, "Rice care");
        product(2, "Rice care plus");
        product(3, "Other treatment");
        disease(100, "Rice blast");
        disease(101, "Leaf blast");
        treatment(200, 1, 100);
        treatment(201, 1, 101);
        treatment(202, 3, 100);
        flush();
        var page = search.execute(new PublicProductQuery(" RICE CARE ", "BLAST", 0));
        assertThat(page.items()).extracting(item -> item.id()).containsExactly(id(1));
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(search.execute(new PublicProductQuery("Rice", null, 0)).totalElements()).isEqualTo(2);
        assertThat(search.execute(new PublicProductQuery(null, "blast", 0)).totalElements()).isEqualTo(2);
        assertThat(search.execute(new PublicProductQuery(null, null, "leaf", 0)).items())
                .extracting(item -> item.id()).containsExactly(id(1));
        assertThat(search.execute(new PublicProductQuery(null, null, "other", 0)).items())
                .extracting(item -> item.id()).containsExactly(id(3));
    }

    @Test
    void pagesAtDatabaseAndTreatsWildcardCharactersLiterally() {
        for (int i = 1; i <= 12; i++) product(i, "Product " + i);
        product(13, "100% organic");
        flush();
        var first = search.execute(new PublicProductQuery(null, null, 0));
        var second = search.execute(new PublicProductQuery(null, null, 1));
        assertThat(first.items()).hasSize(10);
        assertThat(first.totalElements()).isEqualTo(13);
        assertThat(second.items()).hasSize(3);
        assertThat(second.items()).extracting(item -> item.id()).doesNotContainAnyElementsOf(
                first.items().stream().map(item -> item.id()).toList());
        assertThat(search.execute(new PublicProductQuery("%", null, 0)).items()).hasSize(1);
        assertThat(search.execute(new PublicProductQuery("_", null, 0)).items()).isEmpty();
    }

    @Test
    void excludesUnavailableProductsDiseasesAndDeletedTreatments() {
        product(1, "Visible");
        product(2, "Draft").setPublicationStatus("DRAFT");
        product(3, "Locked").setModerationStatus("LOCKED");
        product(4, "Deleted").setDeletedAt(NOW);
        product(5, "Unpublished").setPublicationStatus("UNPUBLISHED");
        disease(100, "Hidden disease").setReviewStatus("HIDDEN");
        disease(101, "Deleted disease").setDeletedAt(NOW);
        disease(102, "Removed treatment");
        treatment(200, 1, 100);
        treatment(201, 1, 101);
        treatment(202, 1, 102).setDeletedAt(NOW);
        flush();
        assertThat(search.execute(new PublicProductQuery(null, null, 0)).items())
                .extracting(item -> item.id()).containsExactly(id(1));
        for (String name : new String[] { "Hidden", "Deleted", "Removed" }) {
            assertThat(search.execute(new PublicProductQuery(null, name, 0)).items()).isEmpty();
        }
    }

    @Test
    void farmerAndOtherBrandCanReadPublishedDetailsButNotDrafts() throws Exception {
        product(1, "Rice care").setSafetyInstruction("Use according to the label");
        product(2, "Private draft").setPublicationStatus("DRAFT");
        disease(100, "Rice blast");
        treatment(200, 1, 100);
        flush();
        for (String role : new String[] { "FARMER", "BRAND" }) {
            mvc.perform(get("/public/products/{id}", id(1)).with(user("other-user").roles(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.product.name").value("Rice care"))
                    .andExpect(jsonPath("$.result.product.safetyInstruction").value("Use according to the label"))
                    .andExpect(jsonPath("$.result.treatments[0].diseaseName").value("Rice blast"));
            mvc.perform(get("/public/products/{id}", id(2)).with(user("other-user").roles(role)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void validatesSearchParametersAndReturnsPageEnvelope() throws Exception {
        product(1, "Rice care");
        flush();
        mvc.perform(get("/public/products/page").param("name", "rice"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.size").value(10))
                .andExpect(jsonPath("$.result.totalElements").value(1));
        mvc.perform(get("/public/products/page").param("page", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VAL_INVALID_REQUEST_PARAMETER"));
        mvc.perform(get("/public/products/page").param("name", "x".repeat(256)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/public/products/page").param("keyword", "x".repeat(256)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/public/products/not-a-uuid")).andExpect(status().isBadRequest());
    }

    private JpaProductEntity product(int index, String name) {
        var product = JpaProductEntity.builder().id(id(index)).brandId(id(500 + index)).categoryId(id(900))
                .name(name).slug("catalog-product-" + index).publicationStatus("PUBLISHED").moderationStatus("NORMAL")
                .publishedAt(NOW).createdAt(NOW).createdBy(id(500)).build();
        entityManager.persist(product);
        return product;
    }

    private JpaDiseaseEntity disease(int index, String name) {
        var disease = JpaDiseaseEntity.builder().id(id(index)).createdSource("ADMIN").name(name)
                .slug("catalog-disease-" + index).cropTypeId(id(901)).reviewStatus("APPROVED")
                .createdAt(NOW).createdBy(id(500)).build();
        entityManager.persist(disease);
        return disease;
    }

    private JpaProductDiseaseTreatmentEntity treatment(int index, int product, int disease) {
        var treatment = JpaProductDiseaseTreatmentEntity.builder().id(id(index)).productId(id(product))
                .diseaseId(id(disease)).brandId(id(500 + product)).effectivenessLevel("HIGH")
                .createdAt(NOW).createdBy(id(500)).build();
        entityManager.persist(treatment);
        return treatment;
    }

    private void flush() { entityManager.flush(); entityManager.clear(); }
    private static UUID id(int index) { return new UUID(1, index); }
}
