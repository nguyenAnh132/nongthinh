import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  afterNextRender,
  inject,
  viewChild,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin, map, switchMap, throwError } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeView,
  DiseaseView,
} from '../../../core/api/agri-catalog-api.service';
import {
  DiagnosisApiService,
  DiagnosisGroup,
  DiagnosisHistoryDetail,
  DiagnosisResult,
  HistorySummary,
} from '../../../core/api/diagnosis-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import { RecommendedProducts } from './recommended-products';

interface LocalPreview {
  file: File;
  url: string;
}

@Component({
  selector: 'app-farmer-diagnosis',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe, RecommendedProducts],
  templateUrl: './diagnosis.html',
  styleUrl: './diagnosis.scss',
})
export class FarmerDiagnosis implements OnDestroy {
  private static readonly MAX_FILES = 5;
  private static readonly MAX_FILE_BYTES = 5 * 1024 * 1024;
  private static readonly ALLOWED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

  private readonly formBuilder = inject(FormBuilder);
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly fileApi = inject(FileApiService);
  private readonly diagnosisApi = inject(DiagnosisApiService);
  private readonly toast = inject(ToastService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly resultPanel = viewChild<ElementRef<HTMLElement>>('resultPanel');
  private readonly historyModal = viewChild<ElementRef<HTMLElement>>('historyModal');
  private readonly imageLightbox = viewChild<ElementRef<HTMLElement>>('imageLightbox');
  private readonly diseaseModal = viewChild<ElementRef<HTMLElement>>('diseaseModal');
  private readonly resultPreviews = new Map<string, LocalPreview>();
  private readonly historyImageUrls = new Map<string, string>();
  private readonly historyThumbnailUrls = new Map<string, string>();
  private readonly loadingHistoryImageIds = new Set<string>();
  private readonly loadingHistoryThumbnailIds = new Set<string>();
  private historyRequestVersion = 0;
  private historySelectionVersion = 0;
  private diseaseRequestVersion = 0;

  readonly scanForm = this.formBuilder.nonNullable.group({
    cropTypeId: ['', Validators.required],
  });

  cropTypes: CropTypeView[] = [];
  previews: LocalPreview[] = [];
  history: HistorySummary[] = [];
  result: DiagnosisResult | null = null;
  recommendationSnapshot: DiagnosisResult | null = null;
  historyDetail: DiagnosisHistoryDetail | null = null;
  selectedDisease: DiseaseView | null = null;
  loadingCrops = true;
  loadingHistory = true;
  loadingHistoryDetail = false;
  scanning = false;
  loadingDisease = false;
  diseaseModalOpen = false;
  historyModalOpen = false;
  selectedHistoryId: string | null = null;
  selectedHistory: HistorySummary | null = null;
  zoomedHistoryImage: DiagnosisResult['images'][number] | null = null;
  imageZoom = 1;
  cropLoadError = '';
  scanError = '';
  historyLoadError = '';
  historyDetailError = '';
  diseaseDetailError = '';

  constructor() {
    afterNextRender(() => {
      this.loadCropTypes();
      this.loadHistory();
    });
  }

  ngOnDestroy(): void {
    this.historyRequestVersion += 1;
    this.historySelectionVersion += 1;
    this.diseaseRequestVersion += 1;
    this.clearPreviews();
    this.clearHistoryImageUrls();
    this.clearHistoryThumbnailUrls();
  }

  @HostListener('document:keydown.escape')
  closeModalOnEscape(): void {
    if (this.recommendationSnapshot) {
      this.recommendationSnapshot = null;
      return;
    }
    if (this.zoomedHistoryImage) {
      this.closeHistoryImage();
      return;
    }
    if (this.diseaseModalOpen) {
      this.closeDiseaseDetail();
      return;
    }
    if (this.historyModalOpen) this.closeHistoryDetail();
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    input.value = '';
    if (!files.length) return;

    const accepted: LocalPreview[] = [];
    let firstError = '';
    for (const file of files) {
      if (this.previews.length + accepted.length >= FarmerDiagnosis.MAX_FILES) {
        firstError ||= 'Mỗi lần chẩn đoán chỉ nhận tối đa 5 ảnh.';
        break;
      }
      if (!FarmerDiagnosis.ALLOWED_TYPES.has(file.type)) {
        firstError ||= 'Chỉ hỗ trợ ảnh JPEG, PNG hoặc WebP.';
        continue;
      }
      if (file.size <= 0 || file.size > FarmerDiagnosis.MAX_FILE_BYTES) {
        firstError ||= 'Mỗi ảnh phải có dung lượng từ 1 byte đến 5 MB.';
        continue;
      }
      if (
        this.previews.some((item) => this.sameFile(item.file, file)) ||
        accepted.some((item) => this.sameFile(item.file, file))
      ) {
        firstError ||= 'Một ảnh đã được chọn trước đó.';
        continue;
      }
      accepted.push({ file, url: URL.createObjectURL(file) });
    }

    this.previews = [...this.previews, ...accepted];
    this.result = null;
    this.historyDetail = null;
    this.historyDetailError = '';
    this.selectedHistoryId = null;
    this.clearHistoryImageUrls();
    this.closeDiseaseDetail();
    if (firstError) this.toast.error(firstError);
  }

  removePreview(index: number): void {
    const preview = this.previews[index];
    if (!preview || this.scanning) return;
    URL.revokeObjectURL(preview.url);
    this.previews = this.previews.filter((_, candidateIndex) => candidateIndex !== index);
    this.result = null;
  }

  clearSelectedFiles(): void {
    if (this.scanning) return;
    this.clearPreviews();
    this.result = null;
    this.historyDetail = null;
    this.historyDetailError = '';
    this.selectedHistoryId = null;
    this.clearHistoryImageUrls();
  }

  scan(): void {
    this.scanError = '';
    this.historyDetail = null;
    this.historyDetailError = '';
    this.selectedHistoryId = null;
    this.clearHistoryImageUrls();
    this.closeDiseaseDetail();
    if (this.scanForm.invalid || !this.previews.length) {
      this.scanForm.markAllAsTouched();
      this.scanError = this.previews.length
        ? 'Vui lòng chọn loại cây trồng trước khi chẩn đoán.'
        : 'Vui lòng chọn từ 1 đến 5 ảnh cây trồng.';
      return;
    }

    const cropTypeId = this.scanForm.controls.cropTypeId.value;
    const submittedPreviews = [...this.previews];
    this.scanning = true;
    forkJoin(
      submittedPreviews.map((preview) => this.fileApi.upload(preview.file, 'DIAGNOSIS_IMAGE')),
    )
      .pipe(
        map((responses) => {
          const uploadedFiles = responses.map((response) => unwrapApiResult<FileView>(response));
          if (uploadedFiles.some((file) => !file?.id)) {
            throw new Error('Tải ảnh lên không hoàn tất. Vui lòng thử lại.');
          }
          const files = uploadedFiles as FileView[];
          this.resultPreviews.clear();
          files.forEach((file, index) =>
            this.resultPreviews.set(file.id, submittedPreviews[index]!),
          );
          return files.map((file) => file.id);
        }),
        switchMap((fileIds) => this.diagnosisApi.create({ cropTypeId, fileIds })),
        finalize(() => {
          this.scanning = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          const result = unwrapApiResult<DiagnosisResult>(response);
          if (!result) {
            this.scanError = 'Máy chủ không trả về kết quả chẩn đoán hợp lệ.';
            return;
          }
          this.result = result;
          this.prependResultToHistory(result);
          this.scrollToResult();
          this.toast.success('Đã hoàn tất chẩn đoán ảnh cây trồng.');
        },
        error: (error) => {
          this.scanError = apiErrorMessage(
            error,
            'Không thể hoàn tất chẩn đoán. Vui lòng thử lại.',
          );
        },
      });
  }

  showDiseaseDetail(group: DiagnosisGroup): void {
    const diseaseId = group.disease?.id;
    if (!diseaseId || this.loadingDisease) return;
    const requestVersion = ++this.diseaseRequestVersion;
    this.diseaseModalOpen = true;
    this.loadingDisease = true;
    this.diseaseDetailError = '';
    this.selectedDisease = null;
    this.focusDiseaseModal();
    this.catalogApi.getPublishedDisease(diseaseId)
      .pipe(
        finalize(() => {
          if (requestVersion !== this.diseaseRequestVersion) return;
          this.loadingDisease = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (disease) => {
          if (requestVersion !== this.diseaseRequestVersion) return;
          const diseaseDetail = unwrapApiResult<DiseaseView>(disease);
          if (!diseaseDetail) {
            this.diseaseDetailError = 'Không thể tải chi tiết bệnh được phát hiện.';
            return;
          }
          this.selectedDisease = diseaseDetail;
        },
        error: (error) => {
          if (requestVersion !== this.diseaseRequestVersion) return;
          this.diseaseDetailError = apiErrorMessage(
            error,
            'Không thể tải thông tin bệnh.',
          );
        },
      });
  }

  selectHistory(item: HistorySummary, showRecommendations = false): void {
    if (this.loadingHistoryDetail) return;
    const requestVersion = ++this.historySelectionVersion;
    this.historyModalOpen = true;
    this.loadingHistoryDetail = true;
    this.historyDetailError = '';
    this.historyDetail = null;
    this.selectedHistoryId = item.id;
    this.selectedHistory = item;
    this.clearHistoryImageUrls();
    this.closeDiseaseDetail();
    this.focusHistoryModal();
    this.diagnosisApi
      .getHistory(item.id)
      .pipe(
        finalize(() => {
          if (requestVersion !== this.historySelectionVersion) return;
          this.loadingHistoryDetail = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          if (requestVersion !== this.historySelectionVersion) return;
          const rawDetail = unwrapApiResult<unknown>(response);
          const detail = this.normalizeHistoryDetail(rawDetail, item);
          if (!detail) {
            this.historyDetailError = 'Không thể đọc snapshot lịch sử chẩn đoán.';
            return;
          }
          this.historyDetail = detail;
          if (showRecommendations) this.recommendationSnapshot = detail.snapshot;
          this.loadHistoryImages(detail.snapshot, requestVersion);
        },
        error: (error) => {
          if (requestVersion !== this.historySelectionVersion) return;
          this.historyDetailError = apiErrorMessage(error, 'Không thể tải lịch sử chẩn đoán.');
        },
      });
  }

  showSelectedDiseaseProducts(): void {
    const snapshot = this.historyModalOpen ? this.historyDetail?.snapshot : this.result;
    if (!snapshot || !this.selectedDisease) return;
    this.recommendationSnapshot = {
      ...snapshot,
      groups: snapshot.groups.filter(group => group.disease?.id === this.selectedDisease?.id),
    };
  }

  retryHistoryDetail(): void {
    if (this.selectedHistory) this.selectHistory(this.selectedHistory);
  }

  openHistoryImage(image: DiagnosisResult['images'][number]): void {
    if (!this.imageUrlForFile(image.file.id)) return;
    this.zoomedHistoryImage = image;
    this.imageZoom = 1;
    requestAnimationFrame(() => this.imageLightbox()?.nativeElement.focus());
  }

  closeHistoryImage(): void {
    this.zoomedHistoryImage = null;
    this.imageZoom = 1;
  }

  zoomHistoryImageIn(): void {
    this.setImageZoom(this.imageZoom + 0.25);
  }

  zoomHistoryImageOut(): void {
    this.setImageZoom(this.imageZoom - 0.25);
  }

  resetHistoryImageZoom(): void {
    this.imageZoom = 1;
  }

  onHistoryImageWheel(event: WheelEvent): void {
    event.preventDefault();
    this.setImageZoom(this.imageZoom + (event.deltaY < 0 ? 0.25 : -0.25));
  }

  closeHistoryDetail(): void {
    this.historySelectionVersion += 1;
    this.closeHistoryImage();
    this.historyModalOpen = false;
    this.loadingHistoryDetail = false;
    this.historyDetail = null;
    this.historyDetailError = '';
    this.selectedHistoryId = null;
    this.selectedHistory = null;
    this.clearHistoryImageUrls();
  }

  imageUrlForFile(fileId: string): string | null {
    if (this.historyDetail) return this.historyImageUrls.get(fileId) ?? null;
    return this.resultPreviews.get(fileId)?.url ?? null;
  }

  isHistoryImageLoading(fileId: string): boolean {
    return this.loadingHistoryImageIds.has(fileId);
  }

  historyThumbnailUrl(historyId: string): string | null {
    return this.historyThumbnailUrls.get(historyId) ?? null;
  }

  isHistoryThumbnailLoading(historyId: string): boolean {
    return this.loadingHistoryThumbnailIds.has(historyId);
  }

  cropName(cropTypeId: string): string {
    return this.cropTypes.find((crop) => crop.id === cropTypeId)?.name ?? 'Loại cây trồng đã lưu';
  }

  statusLabel(status: string): string {
    return (
      {
        DISEASED: 'Có dấu hiệu bệnh',
        HEALTHY: 'Không phát hiện bệnh',
        UNDETERMINED: 'Chưa xác định',
      }[status] ?? status
    );
  }

  formatBytes(value: number): string {
    if (value < 1024) return `${value} B`;
    if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
    return `${(value / (1024 * 1024)).toFixed(1)} MB`;
  }

  closeDiseaseDetail(): void {
    this.diseaseRequestVersion += 1;
    this.diseaseModalOpen = false;
    this.loadingDisease = false;
    this.selectedDisease = null;
    this.diseaseDetailError = '';
  }

  loadCropTypes(): void {
    this.loadingCrops = true;
    this.cropLoadError = '';
    this.catalogApi
      .listActiveCropTypes()
      .pipe(
        finalize(() => {
          this.loadingCrops = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.cropTypes = unwrapApiResult<CropTypeView[]>(response) ?? [];
          if (!this.cropTypes.length) {
            this.cropLoadError = 'Chưa có loại cây trồng nào đang hoạt động để chẩn đoán.';
          }
        },
        error: (error) => {
          this.cropLoadError = apiErrorMessage(error, 'Không thể tải loại cây trồng.');
        },
      });
  }

  loadHistory(): void {
    const requestVersion = ++this.historyRequestVersion;
    this.loadingHistory = true;
    this.historyLoadError = '';
    this.diagnosisApi
      .listHistory()
      .pipe(
        finalize(() => {
          if (requestVersion === this.historyRequestVersion) {
            this.loadingHistory = false;
            this.changeDetectorRef.markForCheck();
          }
        }),
      )
      .subscribe({
        next: (response) => {
          if (requestVersion !== this.historyRequestVersion) return;
          const history = unwrapApiResult<HistorySummary[]>(response);
          if (!history) {
            this.historyLoadError = 'Máy chủ không trả về danh sách lịch sử hợp lệ.';
            return;
          }
          this.history = history;
          this.loadHistoryThumbnails(history, requestVersion);
        },
        error: (error) => {
          if (requestVersion !== this.historyRequestVersion) return;
          this.historyLoadError = apiErrorMessage(error, 'Không thể tải lại lịch sử chẩn đoán.');
        },
      });
  }

  private prependResultToHistory(result: DiagnosisResult): void {
    // Prevent an older request from overwriting the diagnosis that just completed.
    this.historyRequestVersion += 1;
    this.loadingHistory = false;
    this.loadingHistoryThumbnailIds.clear();
    const latest: HistorySummary = {
      id: result.diagnosisId,
      cropTypeId: result.cropTypeId,
      modelId: result.model.id,
      modelVersionId: result.model.versionId,
      modelVersion: result.model.version,
      status: result.status,
      createdAt: new Date().toISOString(),
    };
    this.history = [latest, ...this.history.filter((item) => item.id !== latest.id)].slice(0, 10);
    const firstImage = result.images[0];
    if (firstImage) {
      this.loadHistoryThumbnailFile(latest.id, firstImage.file.id, this.historyRequestVersion);
    }
  }

  private scrollToResult(): void {
    requestAnimationFrame(() => {
      this.resultPanel()?.nativeElement.scrollIntoView?.({ behavior: 'smooth', block: 'start' });
    });
  }

  private normalizeHistoryDetail(
    payload: unknown,
    fallbackHistory: HistorySummary,
  ): DiagnosisHistoryDetail | null {
    const decodedPayload = this.decodeJsonValue(payload);
    const detailRecord = this.asRecord(decodedPayload);
    if (!detailRecord) return null;

    const historyRecord = this.asRecord(this.decodeJsonValue(detailRecord['history']));
    const history: HistorySummary = historyRecord
      ? ({ ...fallbackHistory, ...historyRecord } as unknown as HistorySummary)
      : fallbackHistory;

    const candidate = this.findDiagnosisSnapshot(decodedPayload);
    if (!candidate) return null;
    const modelRecord = this.asRecord(candidate['model']);
    const model = modelRecord
      ? (modelRecord as unknown as DiagnosisResult['model'])
      : {
          id: history.modelId,
          name: 'Model đã lưu',
          versionId: history.modelVersionId,
          version: history.modelVersion,
        };
    const images = this.decodeJsonValue(candidate['images']);
    const groups = this.decodeJsonValue(candidate['groups']);

    return {
      history,
      snapshot: {
        diagnosisId: String(candidate['diagnosisId'] ?? candidate['diagnosis_id'] ?? history.id),
        cropTypeId: String(
          candidate['cropTypeId'] ?? candidate['crop_type_id'] ?? history.cropTypeId,
        ),
        model,
        status: (candidate['status'] ?? history.status) as DiagnosisResult['status'],
        images: Array.isArray(images) ? images : [],
        groups: Array.isArray(groups) ? groups : [],
        processingTimeMs: Number(
          candidate['processingTimeMs'] ?? candidate['processing_time_ms'] ?? 0,
        ),
      },
    };
  }

  private decodeJsonValue(value: unknown): unknown {
    let decoded = value;
    for (let depth = 0; depth < 5 && typeof decoded === 'string'; depth += 1) {
      try {
        decoded = JSON.parse(decoded);
      } catch {
        break;
      }
    }
    return decoded;
  }

  private asRecord(value: unknown): Record<string, unknown> | null {
    return value != null && typeof value === 'object' && !Array.isArray(value)
      ? (value as Record<string, unknown>)
      : null;
  }

  private findDiagnosisSnapshot(value: unknown, depth = 0): Record<string, unknown> | null {
    if (depth > 8) return null;
    const decoded = this.decodeJsonValue(value);
    const record = this.asRecord(decoded);
    if (record) {
      const hasDiagnosisIdentity =
        'diagnosisId' in record || 'diagnosis_id' in record || 'processingTimeMs' in record;
      const hasDiagnosisCollections =
        Array.isArray(record['images']) || Array.isArray(record['groups']);
      const hasDiagnosisModel = this.asRecord(record['model']) != null;
      if (hasDiagnosisIdentity || (hasDiagnosisModel && hasDiagnosisCollections)) return record;

      const entries = Object.entries(record).sort(([left], [right]) => {
        const priority = (key: string) =>
          /^(snapshot|resultSnapshot|result_snapshot|result|value|data|payload)$/i.test(key)
            ? 0
            : 1;
        return priority(left) - priority(right);
      });
      for (const [, nested] of entries) {
        const found = this.findDiagnosisSnapshot(nested, depth + 1);
        if (found) return found;
      }
      return null;
    }

    if (Array.isArray(decoded)) {
      for (const nested of decoded) {
        const found = this.findDiagnosisSnapshot(nested, depth + 1);
        if (found) return found;
      }
    }
    return null;
  }

  private focusDiseaseModal(): void {
    requestAnimationFrame(() => this.diseaseModal()?.nativeElement.focus());
  }

  private focusHistoryModal(): void {
    requestAnimationFrame(() => this.historyModal()?.nativeElement.focus());
  }

  private setImageZoom(value: number): void {
    this.imageZoom = Math.min(4, Math.max(1, Math.round(value * 100) / 100));
  }

  private loadHistoryImages(diagnosis: DiagnosisResult, requestVersion: number): void {
    for (const image of diagnosis.images) {
      this.loadingHistoryImageIds.add(image.file.id);
      this.fileApi.getContent(image.file.id).subscribe({
        next: (blob) => {
          if (requestVersion !== this.historySelectionVersion) return;
          this.loadingHistoryImageIds.delete(image.file.id);
          const previousUrl = this.historyImageUrls.get(image.file.id);
          if (previousUrl) URL.revokeObjectURL(previousUrl);
          this.historyImageUrls.set(image.file.id, URL.createObjectURL(blob));
          this.changeDetectorRef.markForCheck();
        },
        error: () => {
          if (requestVersion === this.historySelectionVersion) {
            this.loadingHistoryImageIds.delete(image.file.id);
            this.changeDetectorRef.markForCheck();
          }
        },
      });
    }
  }

  private loadHistoryThumbnails(history: HistorySummary[], requestVersion: number): void {
    this.clearHistoryThumbnailUrls();
    for (const item of history) {
      this.loadingHistoryThumbnailIds.add(item.id);
      this.diagnosisApi.getHistory(item.id).subscribe({
        next: (response) => {
          if (requestVersion !== this.historyRequestVersion) return;
          const detail = this.normalizeHistoryDetail(unwrapApiResult<unknown>(response), item);
          const firstImage = detail?.snapshot.images[0];
          if (!firstImage) {
            this.finishHistoryThumbnail(item.id);
            return;
          }
          this.loadHistoryThumbnailFile(item.id, firstImage.file.id, requestVersion);
        },
        error: () => {
          if (requestVersion === this.historyRequestVersion) {
            this.finishHistoryThumbnail(item.id);
          }
        },
      });
    }
  }

  private loadHistoryThumbnailFile(
    historyId: string,
    fileId: string,
    requestVersion: number,
  ): void {
    this.loadingHistoryThumbnailIds.add(historyId);
    this.fileApi.getContent(fileId).subscribe({
      next: (blob) => {
        if (requestVersion !== this.historyRequestVersion) return;
        const previousUrl = this.historyThumbnailUrls.get(historyId);
        if (previousUrl) URL.revokeObjectURL(previousUrl);
        this.historyThumbnailUrls.set(historyId, URL.createObjectURL(blob));
        this.finishHistoryThumbnail(historyId);
      },
      error: () => {
        if (requestVersion === this.historyRequestVersion) {
          this.finishHistoryThumbnail(historyId);
        }
      },
    });
  }

  private finishHistoryThumbnail(historyId: string): void {
    this.loadingHistoryThumbnailIds.delete(historyId);
    this.changeDetectorRef.markForCheck();
  }

  private clearHistoryImageUrls(): void {
    this.historyImageUrls.forEach((url) => URL.revokeObjectURL(url));
    this.historyImageUrls.clear();
    this.loadingHistoryImageIds.clear();
  }

  private clearHistoryThumbnailUrls(): void {
    this.historyThumbnailUrls.forEach((url) => URL.revokeObjectURL(url));
    this.historyThumbnailUrls.clear();
    this.loadingHistoryThumbnailIds.clear();
  }

  private clearPreviews(): void {
    this.previews.forEach((preview) => URL.revokeObjectURL(preview.url));
    this.previews = [];
  }

  private sameFile(left: File, right: File): boolean {
    return (
      left.name === right.name &&
      left.size === right.size &&
      left.lastModified === right.lastModified
    );
  }
}
