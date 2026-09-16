import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';

export type DiseaseReviewStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'APPROVED'
  | 'HIDDEN'
  | 'REJECTED';

export type DiseaseCreatedSource = 'ADMIN' | 'BRAND';
export type EffectivenessLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
export type HistoryActorType = 'BRAND' | 'ADMIN' | 'SYSTEM';
export type DiseaseReviewAction =
  | 'CREATED'
  | 'SUBMITTED'
  | 'APPROVED'
  | 'REJECTED'
  | 'HIDDEN'
  | 'RESTORED';
export type ProductHistoryAction =
  | 'CREATED'
  | 'UPDATED'
  | 'PUBLISHED'
  | 'UNPUBLISHED'
  | 'LOCKED'
  | 'UNLOCKED'
  | 'DELETED'
  | 'RESTORED';

export interface DiseasePayload {
  name: string;
  slug: string;
  scientificName: string | null;
  cropTypeId: string;
  affectedPart: string | null;
  pathogenType: string | null;
  shortDescription: string | null;
  description: string | null;
  symptoms: string | null;
  causes: string | null;
  favorableConditions: string | null;
  preventionMethod: string | null;
  treatmentGuideline: string | null;
  thumbnailUrl: string | null;
}

export interface DiseaseView extends DiseasePayload {
  id: string;
  createdSource: DiseaseCreatedSource;
  brandId: string | null;
  reviewStatus: DiseaseReviewStatus;
  rejectionReason: string | null;
  submittedAt: string | null;
  reviewedAt: string | null;
  reviewedBy: string | null;
  publishedAt: string | null;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

export interface DiseaseReviewHistoryView {
  id: string;
  diseaseId: string;
  action: DiseaseReviewAction;
  previousStatus: DiseaseReviewStatus | null;
  newStatus: DiseaseReviewStatus;
  comment: string | null;
  actorId: string;
  actorType: HistoryActorType;
  createdAt: string;
}

export interface DiseaseReviewHistoryListItemView extends DiseaseReviewHistoryView {
  diseaseName: string;
  cropTypeId: string;
  createdSource: DiseaseCreatedSource;
  brandId: string | null;
}

export interface PageView<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface DiseaseReviewHistoryFilters {
  keyword?: string | null;
  action?: DiseaseReviewAction | null;
  newStatus?: DiseaseReviewStatus | null;
  actorType?: HistoryActorType | null;
  from?: string | null;
  to?: string | null;
  diseaseId?: string | null;
  brandId?: string | null;
  actorId?: string | null;
  page?: number | null;
  size?: number | null;
}

export interface ProductDiseaseTreatmentPayload {
  diseaseId: string;
  effectivenessLevel: EffectivenessLevel | null;
  priority: number;
  dosage: string | null;
  applicationMethod: string | null;
  applicationTiming: string | null;
  frequencyInstruction: string | null;
  treatmentNote: string | null;
}

export interface ProductDiseaseTreatmentView
  extends ProductDiseaseTreatmentPayload {
  id: string;
  productId: string;
  brandId: string;
}

export interface ProductCategoryView {
  id: string;
  parentId: string | null;
  name: string;
  slug: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
}

export interface CropTypeView {
  id: string;
  code: string;
  name: string;
  description: string | null;
  active: boolean;
}

export interface CropTypeCreationPayload {
  code: string;
  name: string;
  description: string | null;
}

export interface CropTypeUpdatePayload {
  name: string;
  description: string | null;
  active: boolean;
}

export interface ProductCategoryCreationPayload {
  parentId: string | null;
  name: string;
  slug: string;
  description: string | null;
  displayOrder: number;
}

export interface ProductCategoryUpdatePayload
  extends ProductCategoryCreationPayload {
  active: boolean;
}

export interface ProductCreationPayload {
  brandId: string;
  categoryId: string;
  name: string;
  slug: string;
  sku: string | null;
  registrationNumber: string | null;
  manufacturerName: string | null;
  originCountry: string | null;
  shortDescription: string | null;
  description: string | null;
  ingredients: string | null;
  usageInstruction: string | null;
  dosageInstruction: string | null;
  safetyInstruction: string | null;
  storageInstruction: string | null;
  warning: string | null;
  form: string | null;
  unit: string | null;
  packageSpecification: string | null;
  purchaseUrl: string | null;
}

export interface ProductView extends ProductCreationPayload {
  id: string;
  thumbnailUrl: string | null;
  publicationStatus: string;
  moderationStatus: string;
  featured: boolean;
  averageRating: number;
  reviewCount: number;
  createdAt: string;
  updatedAt: string;
  moderationReason?: string | null;
}

export interface PublicProductDetail {
  product: ProductView;
  categoryName: string | null;
  treatments: { diseaseName: string; treatment: ProductDiseaseTreatmentView }[];
}

export type ProductUpdatePayload = Omit<ProductCreationPayload, 'brandId'>;
export type ProductPublicationStatus = 'PUBLISHED' | 'UNPUBLISHED';

export interface ProductHistoryView {
  id: string;
  productId: string;
  action: ProductHistoryAction;
  actorId: string;
  actorType: HistoryActorType;
  previousPublicationStatus: string | null;
  newPublicationStatus: string | null;
  previousModerationStatus: string | null;
  newModerationStatus: string | null;
  reason: string | null;
  changeSummary: string | null;
  snapshotData: string | null;
  createdAt: string;
}

export interface ProductImageView {
  id: string;
  productId: string;
  fileId: string;
  imageUrl: string;
  altText: string | null;
  displayOrder: number;
  primary: boolean;
}

export type AiModelTaskType = 'DISEASE_DETECTION';
export type CropCoverageType = 'ALL_CROPS' | 'SELECTED_CROPS';
export type AiModelStatus = 'DRAFT' | 'ACTIVE' | 'RETIRED';
export type AiModelVersionStatus = 'DRAFT' | 'VALIDATING' | 'VALIDATED' | 'READY' | 'ACTIVE' | 'RETIRED';
export type AiModelClassKind = 'DISEASE' | 'HEALTHY';
export type AiModelDeploymentScope = 'ALL_CROPS' | 'CROP';
export type AiModelDeploymentStatus = 'ACTIVE' | 'INACTIVE' | 'RETIRED';

export interface AiModelPayload {
  code: string;
  name: string;
  description: string | null;
  taskType: AiModelTaskType;
  cropCoverageType: CropCoverageType;
  cropTypeIds: string[];
}

export interface AiModelUpdatePayload {
  name: string;
  description: string | null;
  cropCoverageType: CropCoverageType;
  cropTypeIds: string[];
}

export interface AiModelView extends AiModelPayload {
  id: string;
  status: AiModelStatus;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

export interface AiModelVersionClassPayload {
  classIndex: number;
  classCode: string;
  displayName: string;
  classKind: AiModelClassKind;
}

export interface AiModelVersionClassView extends AiModelVersionClassPayload {
  id: string;
}

export interface AiModelVersionPayload {
  version: string;
  artifactFileId: string;
  artifactSha256: string;
  inputWidth: number;
  inputHeight: number;
  classes: AiModelVersionClassPayload[];
}

export interface AiModelVersionView extends AiModelVersionPayload {
  id: string;
  modelId: string;
  status: AiModelVersionStatus;
  validationReport: string | null;
  classes: AiModelVersionClassView[];
  createdAt: string;
  createdBy: string;
  validatedAt: string | null;
  validatedBy: string | null;
  retiredAt: string | null;
  retiredBy: string | null;
}

export interface AiModelDiseaseMappingPayload {
  modelVersionClassId: string;
  cropTypeId: string;
  diseaseId: string;
}

export interface AiModelDiseaseMappingView extends AiModelDiseaseMappingPayload {
  id: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

export interface AiModelDeploymentPayload {
  modelVersionId: string;
  deploymentScope: AiModelDeploymentScope;
  cropTypeId: string | null;
  priority: number;
}

export interface AiModelDeploymentView extends AiModelDeploymentPayload {
  id: string;
  status: AiModelDeploymentStatus;
  createdAt: string;
  createdBy: string;
  activatedAt: string | null;
  activatedBy: string | null;
}

export interface DiseaseRecommendation {
  product: {
    id: string;
    brandId: string;
    name: string;
    slug: string;
    thumbnailUrl: string | null;
    shortDescription?: string | null;
    manufacturerName?: string | null;
    purchaseUrl?: string | null;
  };
  treatment: ProductDiseaseTreatmentView;
  averageRating?: number;
  reviewCount?: number;
}

export interface DiseaseRecommendationPage {
  items: DiseaseRecommendation[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

@Injectable({ providedIn: 'root' })
export class AgriCatalogApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/agri-catalog';

  listActiveCategories(): Observable<ApiResponse<ProductCategoryView[]>> {
    return this.http.get<ApiResponse<ProductCategoryView[]>>(
      `${this.baseUrl}/public/product-categories`,
    );
  }

  listCategories(): Observable<ApiResponse<ProductCategoryView[]>> {
    return this.http.get<ApiResponse<ProductCategoryView[]>>(
      `${this.baseUrl}/product-categories`,
    );
  }

  listActiveCropTypes(): Observable<ApiResponse<CropTypeView[]>> {
    return this.http.get<ApiResponse<CropTypeView[]>>(
      `${this.baseUrl}/public/crop-types`,
    );
  }

  listCropTypes(): Observable<ApiResponse<CropTypeView[]>> {
    return this.http.get<ApiResponse<CropTypeView[]>>(`${this.baseUrl}/crop-types`);
  }

  createCropType(
    payload: CropTypeCreationPayload,
  ): Observable<ApiResponse<CropTypeView>> {
    return this.http.post<ApiResponse<CropTypeView>>(
      `${this.baseUrl}/crop-types`,
      payload,
    );
  }

  updateCropType(
    cropTypeId: string,
    payload: CropTypeUpdatePayload,
  ): Observable<ApiResponse<CropTypeView>> {
    return this.http.put<ApiResponse<CropTypeView>>(
      `${this.baseUrl}/crop-types/${cropTypeId}`,
      payload,
    );
  }

  deleteCropType(cropTypeId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/crop-types/${cropTypeId}`,
    );
  }

  createCategory(
    payload: ProductCategoryCreationPayload,
  ): Observable<ApiResponse<ProductCategoryView>> {
    return this.http.post<ApiResponse<ProductCategoryView>>(
      `${this.baseUrl}/product-categories`,
      payload,
    );
  }

  updateCategory(
    categoryId: string,
    payload: ProductCategoryUpdatePayload,
  ): Observable<ApiResponse<ProductCategoryView>> {
    return this.http.put<ApiResponse<ProductCategoryView>>(
      `${this.baseUrl}/product-categories/${categoryId}`,
      payload,
    );
  }

  deleteCategory(categoryId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/product-categories/${categoryId}`,
    );
  }

  listDiseases(filters?: {
    brandId?: string;
    reviewStatus?: DiseaseReviewStatus;
    cropTypeId?: string;
  }): Observable<ApiResponse<DiseaseView[]>> {
    return this.http.get<ApiResponse<DiseaseView[]>>(
      `${this.baseUrl}/diseases`,
      { params: filters ?? {} },
    );
  }

  listApprovedDiseases(cropTypeId?: string): Observable<ApiResponse<DiseaseView[]>> {
    return this.http.get<ApiResponse<DiseaseView[]>>(
      `${this.baseUrl}/public/diseases`,
      { params: cropTypeId ? { cropTypeId } : {} },
    );
  }

  createDisease(payload: DiseasePayload): Observable<ApiResponse<DiseaseView>> {
    return this.http.post<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases`,
      payload,
    );
  }

  updateDisease(
    diseaseId: string,
    payload: DiseasePayload,
  ): Observable<ApiResponse<DiseaseView>> {
    return this.http.put<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases/${diseaseId}`,
      payload,
    );
  }

  deleteDisease(diseaseId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/diseases/${diseaseId}`,
    );
  }

  submitDisease(diseaseId: string): Observable<ApiResponse<DiseaseView>> {
    return this.http.post<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases/${diseaseId}/submit`,
      {},
    );
  }

  approveDisease(diseaseId: string): Observable<ApiResponse<DiseaseView>> {
    return this.http.post<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases/${diseaseId}/approve`,
      {},
    );
  }

  rejectDisease(
    diseaseId: string,
    reason: string,
  ): Observable<ApiResponse<DiseaseView>> {
    return this.http.post<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases/${diseaseId}/reject`,
      { reason },
    );
  }

  hideDisease(diseaseId: string): Observable<ApiResponse<DiseaseView>> {
    return this.http.post<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases/${diseaseId}/hide`,
      {},
    );
  }

  restoreDisease(diseaseId: string): Observable<ApiResponse<DiseaseView>> {
    return this.http.post<ApiResponse<DiseaseView>>(
      `${this.baseUrl}/diseases/${diseaseId}/restore`,
      {},
    );
  }

  listDiseaseReviewHistory(
    diseaseId: string,
  ): Observable<ApiResponse<DiseaseReviewHistoryView[]>> {
    return this.http.get<ApiResponse<DiseaseReviewHistoryView[]>>(
      `${this.baseUrl}/diseases/${diseaseId}/review-history`,
    );
  }

  listDiseaseReviewHistories(
    filters: DiseaseReviewHistoryFilters = {},
  ): Observable<ApiResponse<PageView<DiseaseReviewHistoryListItemView>>> {
    let params = new HttpParams().set('createdSource', 'BRAND');
    for (const [key, rawValue] of Object.entries(filters)) {
      if (rawValue === undefined || rawValue === null) continue;
      const value = typeof rawValue === 'string' ? rawValue.trim() : String(rawValue);
      if (!value) continue;
      params = params.set(key, value);
    }
    return this.http.get<ApiResponse<PageView<DiseaseReviewHistoryListItemView>>>(
      `${this.baseUrl}/disease-review-histories`,
      { params },
    );
  }

  listProducts(): Observable<ApiResponse<ProductView[]>> {
    return this.http.get<ApiResponse<ProductView[]>>(`${this.baseUrl}/products`);
  }

  searchPublicProducts(keyword = '', page = 0): Observable<ApiResponse<PageView<ProductView>>> {
    return this.http.get<ApiResponse<PageView<ProductView>>>(`${this.baseUrl}/public/products/page`, {
      params: { keyword, page },
    });
  }

  getPublicProduct(productId: string): Observable<ApiResponse<PublicProductDetail>> {
    return this.http.get<ApiResponse<PublicProductDetail>>(`${this.baseUrl}/public/products/${productId}`);
  }

  getProduct(productId: string): Observable<ApiResponse<ProductView>> {
    return this.http.get<ApiResponse<ProductView>>(`${this.baseUrl}/products/${productId}`);
  }

  updateProduct(productId: string, payload: ProductUpdatePayload): Observable<ApiResponse<ProductView>> {
    return this.http.put<ApiResponse<ProductView>>(`${this.baseUrl}/products/${productId}`, payload);
  }

  updateProductPublicationStatus(
    productId: string,
    status: ProductPublicationStatus,
  ): Observable<ApiResponse<ProductView>> {
    const action = status === 'PUBLISHED' ? 'publish' : 'unpublish';
    return this.http.post<ApiResponse<ProductView>>(
      `${this.baseUrl}/products/${productId}/${action}`,
      {},
    );
  }

  listProductDiseaseTreatments(productId: string): Observable<ApiResponse<ProductDiseaseTreatmentView[]>> {
    return this.http.get<ApiResponse<ProductDiseaseTreatmentView[]>>(
      `${this.baseUrl}/products/${productId}/disease-treatments`,
    );
  }

  updateProductDiseaseTreatment(productId: string, treatmentId: string, payload: Omit<ProductDiseaseTreatmentPayload, 'diseaseId'>): Observable<ApiResponse<ProductDiseaseTreatmentView>> {
    return this.http.put<ApiResponse<ProductDiseaseTreatmentView>>(
      `${this.baseUrl}/products/${productId}/disease-treatments/${treatmentId}`, payload,
    );
  }

  deleteProductDiseaseTreatment(productId: string, treatmentId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.baseUrl}/products/${productId}/disease-treatments/${treatmentId}`,
    );
  }

  listProductHistory(
    productId: string,
  ): Observable<ApiResponse<ProductHistoryView[]>> {
    return this.http.get<ApiResponse<ProductHistoryView[]>>(
      `${this.baseUrl}/products/${productId}/history`,
    );
  }

  createProduct(payload: ProductCreationPayload): Observable<ApiResponse<ProductView>> {
    return this.http.post<ApiResponse<ProductView>>(`${this.baseUrl}/products`, payload);
  }

  createProductDiseaseTreatment(
    productId: string,
    payload: ProductDiseaseTreatmentPayload,
  ): Observable<ApiResponse<ProductDiseaseTreatmentView>> {
    return this.http.post<ApiResponse<ProductDiseaseTreatmentView>>(
      `${this.baseUrl}/products/${productId}/disease-treatments`,
      payload,
    );
  }

  attachProductImage(
    productId: string,
    fileId: string,
    altText: string | null,
  ): Observable<ApiResponse<ProductImageView>> {
    return this.http.post<ApiResponse<ProductImageView>>(
      `${this.baseUrl}/products/${productId}/images`,
      {
        fileId,
        altText,
        displayOrder: 0,
        primary: true,
      },
    );
  }

  getPublishedDisease(diseaseId: string): Observable<ApiResponse<DiseaseView>> {
    return this.http.get<ApiResponse<DiseaseView>>(`${this.baseUrl}/public/diseases/${diseaseId}`);
  }

  getDiseaseRecommendations(
    diseaseId: string,
    limit = 3,
  ): Observable<ApiResponse<DiseaseRecommendation[]>> {
    return this.http.get<ApiResponse<DiseaseRecommendation[]>>(
      `${this.baseUrl}/public/diseases/${diseaseId}/recommendations`,
      { params: { limit } },
    );
  }

  listDiseaseRecommendations(
    diseaseId: string,
    page = 0,
  ): Observable<ApiResponse<DiseaseRecommendationPage>> {
    return this.http.get<ApiResponse<DiseaseRecommendationPage>>(
      `${this.baseUrl}/public/diseases/${diseaseId}/recommendations/page`,
      { params: { page } },
    );
  }

  listAiModels(): Observable<ApiResponse<AiModelView[]>> {
    return this.http.get<ApiResponse<AiModelView[]>>(`${this.baseUrl}/ai-models`);
  }

  createAiModel(payload: AiModelPayload): Observable<ApiResponse<AiModelView>> {
    return this.http.post<ApiResponse<AiModelView>>(`${this.baseUrl}/ai-models`, payload);
  }

  updateAiModel(
    modelId: string,
    payload: AiModelUpdatePayload,
  ): Observable<ApiResponse<AiModelView>> {
    return this.http.put<ApiResponse<AiModelView>>(`${this.baseUrl}/ai-models/${modelId}`, payload);
  }

  retireAiModel(modelId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/ai-models/${modelId}`);
  }

  listAiModelVersions(modelId: string): Observable<ApiResponse<AiModelVersionView[]>> {
    return this.http.get<ApiResponse<AiModelVersionView[]>>(
      `${this.baseUrl}/ai-models/${modelId}/versions`,
    );
  }

  createAiModelVersion(
    modelId: string,
    payload: AiModelVersionPayload,
  ): Observable<ApiResponse<AiModelVersionView>> {
    return this.http.post<ApiResponse<AiModelVersionView>>(
      `${this.baseUrl}/ai-models/${modelId}/versions`,
      payload,
    );
  }

  validateAiModelVersion(versionId: string): Observable<ApiResponse<AiModelVersionView>> {
    return this.http.post<ApiResponse<AiModelVersionView>>(
      `${this.baseUrl}/ai-model-versions/${versionId}/validate`,
      {},
    );
  }

  markAiModelVersionReady(versionId: string): Observable<ApiResponse<AiModelVersionView>> {
    return this.http.post<ApiResponse<AiModelVersionView>>(
      `${this.baseUrl}/ai-model-versions/${versionId}/ready`,
      {},
    );
  }

  retireAiModelVersion(versionId: string): Observable<ApiResponse<AiModelVersionView>> {
    return this.http.post<ApiResponse<AiModelVersionView>>(
      `${this.baseUrl}/ai-model-versions/${versionId}/retire`,
      {},
    );
  }

  listAiModelDiseaseMappings(
    modelVersionId: string,
  ): Observable<ApiResponse<AiModelDiseaseMappingView[]>> {
    return this.http.get<ApiResponse<AiModelDiseaseMappingView[]>>(
      `${this.baseUrl}/ai-model-disease-mappings`,
      { params: { modelVersionId } },
    );
  }

  createAiModelDiseaseMapping(
    payload: AiModelDiseaseMappingPayload,
  ): Observable<ApiResponse<AiModelDiseaseMappingView>> {
    return this.http.post<ApiResponse<AiModelDiseaseMappingView>>(
      `${this.baseUrl}/ai-model-disease-mappings`,
      payload,
    );
  }

  updateAiModelDiseaseMapping(
    mappingId: string,
    payload: AiModelDiseaseMappingPayload,
  ): Observable<ApiResponse<AiModelDiseaseMappingView>> {
    return this.http.put<ApiResponse<AiModelDiseaseMappingView>>(
      `${this.baseUrl}/ai-model-disease-mappings/${mappingId}`,
      payload,
    );
  }

  deleteAiModelDiseaseMapping(mappingId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/ai-model-disease-mappings/${mappingId}`);
  }

  listAiModelDeployments(): Observable<ApiResponse<AiModelDeploymentView[]>> {
    return this.http.get<ApiResponse<AiModelDeploymentView[]>>(
      `${this.baseUrl}/ai-model-deployments`,
    );
  }

  createAiModelDeployment(
    payload: AiModelDeploymentPayload,
  ): Observable<ApiResponse<AiModelDeploymentView>> {
    return this.http.post<ApiResponse<AiModelDeploymentView>>(
      `${this.baseUrl}/ai-model-deployments`,
      payload,
    );
  }

  activateAiModelDeployment(
    deploymentId: string,
  ): Observable<ApiResponse<AiModelDeploymentView>> {
    return this.http.post<ApiResponse<AiModelDeploymentView>>(
      `${this.baseUrl}/ai-model-deployments/${deploymentId}/activate`,
      {},
    );
  }

  deactivateAiModelDeployment(
    deploymentId: string,
  ): Observable<ApiResponse<AiModelDeploymentView>> {
    return this.http.post<ApiResponse<AiModelDeploymentView>>(
      `${this.baseUrl}/ai-model-deployments/${deploymentId}/deactivate`,
      {},
    );
  }
}
