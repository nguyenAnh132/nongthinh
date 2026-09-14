package com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseaseRecommendationsUseCase;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.infra.persistence.disease.JpaDiseaseEntity;
import com.nongthinh.agri_catalog_service.infra.persistence.product.JpaProductEntity;
import com.nongthinh.agri_catalog_service.infra.persistence.productreview.JpaProductReviewEntity;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiseaseRecommendationPersistenceTest {
    private static final Instant NOW = Instant.parse("2026-09-13T00:00:00Z");
    private static final UUID DISEASE_ID = id(900);
    private static final UUID ACTOR_ID = id(901);
    private int nextReview = 1000;

    @Autowired private ListDiseaseRecommendationsUseCase useCase;
    @Autowired private EntityManager entityManager;
    @Autowired private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        entityManager.persist(JpaDiseaseEntity.builder().id(DISEASE_ID).createdSource("ADMIN")
                .name("Brown spot").slug("brown-spot-recommendation-test").cropTypeId(id(902))
                .reviewStatus("APPROVED").createdAt(NOW).createdBy(ACTOR_ID).build());
    }

    @Test
    void ranksBeforePaginationByEffectivenessThenVisibleRatingThenReviewCountAndStableId() {
        product(1, "VERY_HIGH"); // Effectiveness takes precedence even with no reviews.
        product(2, "HIGH");
        review(2, 5, "VISIBLE", false);
        review(2, 5, "VISIBLE", false);
        product(3, "HIGH");
        review(3, 5, "VISIBLE", false);
        product(4, "HIGH");
        review(4, 4, "VISIBLE", false);
        review(4, 5, "HIDDEN", false);
        review(4, 5, "VISIBLE", true);
        product(5, "MEDIUM");
        for (int index = 6; index <= 12; index++) product(index, "LOW");
        product(13, null);
        review(13, 5, "VISIBLE", false);
        entityManager.flush();
        entityManager.clear();

        var first = useCase.execute(DISEASE_ID, 0);
        var second = useCase.execute(DISEASE_ID, 1);
        assertThat(first.items()).extracting(item -> item.product().id())
                .containsExactly(id(1), id(2), id(3), id(4), id(5), id(6), id(7), id(8), id(9), id(10));
        assertThat(first.size()).isEqualTo(10);
        assertThat(first.totalElements()).isEqualTo(13);
        assertThat(first.hasNext()).isTrue();
        assertThat(first.items().get(1).averageRating()).isEqualTo(5);
        assertThat(first.items().get(1).reviewCount()).isEqualTo(2);
        assertThat(first.items().get(3).averageRating()).isEqualTo(4);
        assertThat(first.items().get(3).reviewCount()).isEqualTo(1);
        assertThat(second.items()).extracting(item -> item.product().id()).containsExactly(id(11), id(12), id(13));
        assertThat(second.hasNext()).isFalse();
        assertThat(useCase.execute(DISEASE_ID, 2).items()).isEmpty();
    }

    @Test
    void excludesUnavailableProductsDeletedTreatmentsAndOtherDiseases() {
        product(1, "HIGH");
        product(2, "HIGH").setPublicationStatus("DRAFT");
        product(3, "HIGH").setModerationStatus("LOCKED");
        product(4, "HIGH").setDeletedAt(NOW);
        product(5, "HIGH");
        entityManager.find(JpaProductDiseaseTreatmentEntity.class, id(105)).setDeletedAt(NOW);
        entityManager.persist(JpaDiseaseEntity.builder().id(id(903)).createdSource("ADMIN")
                .name("Other disease").slug("other-recommendation-test").cropTypeId(id(902))
                .reviewStatus("APPROVED").createdAt(NOW).createdBy(ACTOR_ID).build());
        product(6, "HIGH");
        entityManager.find(JpaProductDiseaseTreatmentEntity.class, id(106)).setDiseaseId(id(903));
        entityManager.flush();
        entityManager.clear();

        var page = useCase.execute(DISEASE_ID, 0);
        assertThat(page.items()).extracting(item -> item.product().id()).containsExactly(id(1));
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidPageAndUnavailableDisease() {
        assertThatThrownBy(() -> useCase.execute(DISEASE_ID, -1)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> useCase.execute(DISEASE_ID, Integer.MAX_VALUE)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> useCase.execute(id(999), 0)).isInstanceOf(BusinessException.class);
        entityManager.find(JpaDiseaseEntity.class, DISEASE_ID).setReviewStatus("HIDDEN");
        entityManager.flush();
        assertThatThrownBy(() -> useCase.execute(DISEASE_ID, 0)).isInstanceOf(BusinessException.class);
    }

    @Test
    void publicEndpointReturnsPageEnvelopeAndMapsInvalidParameters() throws Exception {
        product(1, "HIGH");
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/public/diseases/{diseaseId}/recommendations/page", DISEASE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items[0].product.id").value(id(1).toString()))
                .andExpect(jsonPath("$.result.items[0].product.brandId").value(ACTOR_ID.toString()))
                .andExpect(jsonPath("$.result.items[0].product.manufacturerName").value("Manufacturer 1"))
                .andExpect(jsonPath("$.result.size").value(10))
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.hasNext").value(false));
        mockMvc.perform(get("/public/diseases/{diseaseId}/recommendations/page", DISEASE_ID).param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_INVALID_REQUEST_PARAMETER"));
        mockMvc.perform(get("/public/diseases/{diseaseId}/recommendations/page", "invalid-id"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/public/diseases/{diseaseId}/recommendations/page", id(999)))
                .andExpect(status().isNotFound());
    }

    private JpaProductEntity product(int index, String effectiveness) {
        var product = JpaProductEntity.builder().id(id(index)).brandId(ACTOR_ID).categoryId(id(904))
                .name("Product " + index).slug("recommendation-product-" + index)
                .manufacturerName("Manufacturer " + index)
                .publicationStatus("PUBLISHED").moderationStatus("NORMAL")
                .createdAt(NOW).createdBy(ACTOR_ID).build();
        entityManager.persist(product);
        entityManager.persist(JpaProductDiseaseTreatmentEntity.builder().id(id(index + 100))
                .productId(product.getId()).diseaseId(DISEASE_ID).brandId(ACTOR_ID)
                .effectivenessLevel(effectiveness).priority(100 - index)
                .createdAt(NOW).createdBy(ACTOR_ID).build());
        return product;
    }

    private void review(int product, int rating, String status, boolean deleted) {
        int index = nextReview++;
        entityManager.persist(JpaProductReviewEntity.builder().id(id(index)).productId(id(product))
                .farmerId(id(index + 1000)).rating((short) rating).status(status)
                .deletedAt(deleted ? NOW : null).createdAt(NOW).createdBy(ACTOR_ID).build());
    }

    private static UUID id(int value) {
        return new UUID(0, value);
    }
}
