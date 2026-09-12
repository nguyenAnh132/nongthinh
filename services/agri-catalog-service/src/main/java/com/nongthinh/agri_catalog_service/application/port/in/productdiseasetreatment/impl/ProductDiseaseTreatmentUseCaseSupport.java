package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ProductDiseaseTreatmentUseCaseSupport {

    private final ProductRepository productRepository;
    private final DiseaseRepository diseaseRepository;
    private final ProductDiseaseTreatmentRepository treatmentRepository;
    private final CurrentUserProvider currentUserProvider;

    Product requireProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    Product requireWritableProduct(UUID productId) {
        Product product = requireProduct(productId);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)
                && !currentUser.getUserId().equals(product.getBrandId())) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
        return product;
    }

    Product requirePublicProduct(UUID productId) {
        Product product = requireProduct(productId);
        if (product.getPublicationStatus() != PublicationStatus.PUBLISHED
                || product.getModerationStatus() != ModerationStatus.NORMAL) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    Disease requireDisease(UUID diseaseId) {
        return diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISEASE_NOT_FOUND));
    }

    Disease requireApprovedDisease(UUID diseaseId) {
        Disease disease = requireDisease(diseaseId);
        if (disease.getReviewStatus() != ReviewStatus.APPROVED) {
            throw new BusinessException(ErrorCode.DISEASE_NOT_APPROVED);
        }
        return disease;
    }

    Disease requirePublicDisease(UUID diseaseId) {
        Disease disease = requireDisease(diseaseId);
        if (disease.getReviewStatus() != ReviewStatus.APPROVED) {
            throw new BusinessException(ErrorCode.DISEASE_NOT_FOUND);
        }
        return disease;
    }

    ProductDiseaseTreatment requireTreatment(UUID productId, UUID treatmentId) {
        ProductDiseaseTreatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PRODUCT_DISEASE_TREATMENT_NOT_FOUND
                ));
        if (!productId.equals(treatment.getProductId())) {
            throw new BusinessException(ErrorCode.PRODUCT_DISEASE_TREATMENT_NOT_FOUND);
        }
        return treatment;
    }
}
