package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;

public record ProductCreationCommand(
        UUID brandId,
        UUID categoryId,
        String name,
        String slug,
        String sku,
        String registrationNumber,
        String manufacturerName,
        String originCountry,
        String shortDescription,
        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,
        String form,
        String unit,
        String packageSpecification,
        String purchaseUrl
) {
}
