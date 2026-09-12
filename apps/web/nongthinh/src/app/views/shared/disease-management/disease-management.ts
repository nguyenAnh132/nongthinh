import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  Input,
  NgZone,
  ViewChild,
  afterNextRender,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeView,
  DiseasePayload,
  DiseaseReviewAction,
  DiseaseReviewHistoryFilters,
  DiseaseReviewHistoryListItemView,
  DiseaseReviewHistoryView,
  DiseaseReviewStatus,
  DiseaseView,
  HistoryActorType,
  PageView,
} from '../../../core/api/agri-catalog-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

type StatusFilter = 'ALL' | DiseaseReviewStatus;
type HistorySelectFilter<T extends string> = '' | T;

interface HistoryExplorerFilters {
  keyword: string;
  action: HistorySelectFilter<DiseaseReviewAction>;
  newStatus: HistorySelectFilter<DiseaseReviewStatus>;
  actorType: HistorySelectFilter<HistoryActorType>;
  fromDate: string;
  toDate: string;
}

@Component({
  selector: 'app-disease-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './disease-management.html',
  styleUrl: './disease-management.scss',
})
export class DiseaseManagement {
  @Input() adminMode = false;
  @ViewChild('historyDialog') historyDialog?: ElementRef<HTMLDialogElement>;

  private readonly fb = inject(FormBuilder);
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly fileApi = inject(FileApiService);
  private readonly toast = inject(ToastService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);
  private readonly historyKeywordChanges = new Subject<string>();
  private recentHistoryRequest?: Subscription;
  private historyPageRequest?: Subscription;

  diseases: DiseaseView[] = [];
  cropTypes: CropTypeView[] = [];
  cropTypesLoading = false;
  loading = true;
  loadError = '';
  saving = false;
  uploadingImage = false;
  editorOpen = false;
  imageFileName = '';
  imageError = '';
  actionId: string | null = null;
  actionMenuId: string | null = null;
  editingId: string | null = null;
  deletingId: string | null = null;
  rejectionId: string | null = null;
  rejectionReason = '';
  searchTerm = '';
  statusFilter: StatusFilter = 'ALL';
  sourceFilter: 'ALL' | 'ADMIN' | 'BRAND' = 'ALL';
  expandedHistoryId: string | null = null;
  historyLoadingId: string | null = null;
  readonly reviewHistories: Record<string, DiseaseReviewHistoryView[]> = {};
  recentHistories: DiseaseReviewHistoryListItemView[] = [];
  recentHistoriesLoading = false;
  recentHistoriesError = '';
  historyExplorerOpen = false;
  historyLoading = false;
  historyError = '';
  historyPage: PageView<DiseaseReviewHistoryListItemView> | null = null;
  historyFilters: HistoryExplorerFilters = this.emptyHistoryFilters();

  readonly diseaseForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    slug: ['', [Validators.required, Validators.maxLength(280)]],
    scientificName: ['', Validators.maxLength(255)],
    cropTypeId: ['', Validators.required],
    affectedPart: ['', Validators.maxLength(100)],
    pathogenType: ['', Validators.maxLength(50)],
    shortDescription: ['', Validators.maxLength(500)],
    description: [''],
    symptoms: [''],
    causes: [''],
    favorableConditions: [''],
    preventionMethod: [''],
    treatmentGuideline: [''],
    thumbnailUrl: ['', Validators.maxLength(2000)],
  });

  get editingDisease(): DiseaseView | null {
    return this.diseases.find((item) => item.id === this.editingId) ?? null;
  }

  get filteredDiseases(): DiseaseView[] {
    const search = this.searchTerm.trim().toLocaleLowerCase('vi');
    return this.diseases.filter((disease) => {
      const matchesStatus =
        this.statusFilter === 'ALL' || disease.reviewStatus === this.statusFilter;
      const matchesSource =
        this.sourceFilter === 'ALL' || disease.createdSource === this.sourceFilter;
      const matchesSearch =
        !search ||
        [
          disease.name,
          disease.scientificName,
          this.cropTypeLabel(disease.cropTypeId),
          disease.affectedPart,
        ].some(
          (value) => value?.toLocaleLowerCase('vi').includes(search),
        );
      return matchesStatus && matchesSource && matchesSearch;
    });
  }

  get pendingCount(): number {
    return this.diseases.filter((disease) => disease.reviewStatus === 'PENDING_REVIEW').length;
  }

  constructor() {
    this.historyKeywordChanges
      .pipe(
        debounceTime(350),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        if (this.historyExplorerOpen) this.loadHistoryPage(0);
      });
    afterNextRender(() => {
      this.loadCropTypes();
      this.loadDiseases();
      if (this.adminMode) this.loadRecentHistories();
    });
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    this.actionMenuId = null;
    if (this.historyExplorerOpen) {
      this.closeHistoryExplorer();
      return;
    }
    if (this.editorOpen && !this.saving && !this.uploadingImage) {
      this.cancelEdit();
    }
  }

  @HostListener('document:click')
  closeActionMenu(): void {
    this.actionMenuId = null;
  }

  toggleActionMenu(diseaseId: string, event: MouseEvent): void {
    event.stopPropagation();
    this.actionMenuId = this.actionMenuId === diseaseId ? null : diseaseId;
  }

  loadDiseases(): void {
    this.loading = true;
    this.loadError = '';
    this.catalogApi
      .listDiseases()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const diseases = unwrapApiResult<DiseaseView[]>(response);
            if (!Array.isArray(diseases)) {
              this.diseases = [];
              this.loadError = apiResponseErrorMessage(
                typeof response === 'object' && response !== null && !Array.isArray(response)
                  ? (response as { message?: string })
                  : null,
                'Máy chủ trả về dữ liệu bệnh cây trồng không hợp lệ.',
              );
              return;
            }

            this.diseases = [...diseases].sort(this.sortDiseases);
          });
        },
        error: (error) => {
          this.syncView(() => {
            this.diseases = [];
            this.loadError = apiErrorMessage(
              error,
              'Không thể tải danh sách bệnh cây trồng. Vui lòng thử lại.',
            );
            this.toast.error(this.loadError);
          });
        },
      });
  }

  loadCropTypes(): void {
    this.cropTypesLoading = true;
    this.catalogApi
      .listActiveCropTypes()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.cropTypesLoading = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const cropTypes = unwrapApiResult<CropTypeView[]>(response);
            this.cropTypes = Array.isArray(cropTypes)
              ? [...cropTypes].sort((left, right) => left.name.localeCompare(right.name, 'vi'))
              : [];
          });
        },
        error: (error) => {
          this.syncView(() => {
            this.cropTypes = [];
            this.toast.error(apiErrorMessage(error, 'Không thể tải danh mục loại cây trồng.'));
          });
        },
      });
  }

  openCreate(): void {
    this.resetForm();
    this.editingId = null;
    this.editorOpen = true;
  }

  submitForm(): void {
    if (this.uploadingImage) {
      this.toast.warning('Vui lòng chờ ảnh đại diện tải lên hoàn tất.');
      return;
    }
    if (this.diseaseForm.invalid) {
      this.diseaseForm.markAllAsTouched();
      return;
    }
    const payload = this.buildPayload();
    const request = this.editingId
      ? this.catalogApi.updateDisease(this.editingId, payload)
      : this.catalogApi.createDisease(payload);

    this.saving = true;
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.saving = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const saved = unwrapApiResult<DiseaseView>(response);
            if (!saved) {
              this.toast.error('Máy chủ không trả về bệnh vừa lưu.');
              return;
            }
            this.upsert(saved);
            this.toast.success(
              this.editingId
                ? `Đã cập nhật bệnh “${saved.name}”.`
                : this.adminMode
                  ? `Đã tạo và công bố bệnh “${saved.name}”.`
                  : `Đã lưu bệnh “${saved.name}” ở trạng thái bản nháp.`,
            );
            this.editingId = null;
            this.editorOpen = false;
            this.resetForm();
          });
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể lưu thông tin bệnh.')),
      });
  }

  startEdit(disease: DiseaseView): void {
    if (!this.canEdit(disease)) return;
    this.editingId = disease.id;
    this.editorOpen = true;
    this.imageFileName = '';
    this.imageError = '';
    this.diseaseForm.reset({
      name: disease.name,
      slug: disease.slug,
      scientificName: disease.scientificName ?? '',
      cropTypeId: disease.cropTypeId,
      affectedPart: disease.affectedPart ?? '',
      pathogenType: disease.pathogenType ?? '',
      shortDescription: disease.shortDescription ?? '',
      description: disease.description ?? '',
      symptoms: disease.symptoms ?? '',
      causes: disease.causes ?? '',
      favorableConditions: disease.favorableConditions ?? '',
      preventionMethod: disease.preventionMethod ?? '',
      treatmentGuideline: disease.treatmentGuideline ?? '',
      thumbnailUrl: disease.thumbnailUrl ?? '',
    });
  }

  cancelEdit(): void {
    if (this.saving || this.uploadingImage) return;
    this.editingId = null;
    this.editorOpen = false;
    this.resetForm();
  }

  private resetForm(): void {
    this.imageFileName = '';
    this.imageError = '';
    this.diseaseForm.reset({
      name: '',
      slug: '',
      scientificName: '',
      cropTypeId: '',
      affectedPart: '',
      pathogenType: '',
      shortDescription: '',
      description: '',
      symptoms: '',
      causes: '',
      favorableConditions: '',
      preventionMethod: '',
      treatmentGuideline: '',
      thumbnailUrl: '',
    });
  }

  submitForReview(disease: DiseaseView): void {
    this.runDiseaseAction(
      disease,
      this.catalogApi.submitDisease(disease.id),
      `Đã gửi bệnh “${disease.name}” để quản trị viên duyệt.`,
    );
  }

  approve(disease: DiseaseView): void {
    this.runDiseaseAction(
      disease,
      this.catalogApi.approveDisease(disease.id),
      `Đã duyệt và công bố bệnh “${disease.name}”.`,
    );
  }

  openReject(disease: DiseaseView): void {
    this.rejectionId = disease.id;
    this.rejectionReason = '';
  }

  reject(disease: DiseaseView): void {
    const reason = this.rejectionReason.trim();
    if (!reason) {
      this.toast.warning('Vui lòng nhập lý do từ chối.');
      return;
    }
    this.runDiseaseAction(
      disease,
      this.catalogApi.rejectDisease(disease.id, reason),
      `Đã từ chối bệnh “${disease.name}”.`,
      () => {
        this.rejectionId = null;
        this.rejectionReason = '';
      },
    );
  }

  hide(disease: DiseaseView): void {
    this.runDiseaseAction(
      disease,
      this.catalogApi.hideDisease(disease.id),
      `Đã ẩn bệnh “${disease.name}” khỏi danh sách công khai.`,
    );
  }

  restore(disease: DiseaseView): void {
    this.runDiseaseAction(
      disease,
      this.catalogApi.restoreDisease(disease.id),
      `Đã khôi phục bệnh “${disease.name}” vào danh sách công khai.`,
    );
  }

  deleteDisease(disease: DiseaseView): void {
    this.actionId = disease.id;
    this.catalogApi
      .deleteDisease(disease.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.actionId = null;
          });
        }),
      )
      .subscribe({
        next: () => {
          this.syncView(() => {
            this.diseases = this.diseases.filter((item) => item.id !== disease.id);
            this.deletingId = null;
            if (this.editingId === disease.id) this.cancelEdit();
            this.toast.success(`Đã xoá bệnh “${disease.name}”.`);
          });
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể xoá bệnh.')),
      });
  }

  canEdit(disease: DiseaseView): boolean {
    return (
      this.adminMode || disease.reviewStatus === 'DRAFT' || disease.reviewStatus === 'REJECTED'
    );
  }

  onNameInput(): void {
    const slug = this.diseaseForm.controls.slug;
    if (!slug.dirty) slug.setValue(this.toSlug(this.diseaseForm.controls.name.value));
  }

  onThumbnailSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.imageError = '';
    if (!file) return;

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.imageError = 'Chỉ chấp nhận ảnh JPEG, PNG hoặc WebP.';
      input.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.imageError = 'Ảnh không được vượt quá 5 MB.';
      input.value = '';
      return;
    }

    this.uploadingImage = true;
    this.fileApi
      .upload(file, 'DISEASE_IMAGE')
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.uploadingImage = false;
            input.value = '';
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const uploaded = unwrapApiResult<FileView>(response);
            const publicUrl = uploaded?.publicUrl?.trim();
            if (!publicUrl) {
              this.imageError = 'Upload thành công nhưng file-service không trả về publicUrl.';
              return;
            }
            this.imageFileName = file.name;
            this.diseaseForm.controls.thumbnailUrl.setValue(publicUrl);
            this.diseaseForm.controls.thumbnailUrl.markAsDirty();
            this.toast.success('Đã tải ảnh đại diện lên.');
          });
        },
        error: (error) => {
          this.syncView(() => {
            this.imageError = apiErrorMessage(error, 'Không thể tải ảnh đại diện lên.');
          });
        },
      });
  }

  removeThumbnail(): void {
    if (this.uploadingImage) return;
    this.imageFileName = '';
    this.imageError = '';
    this.diseaseForm.controls.thumbnailUrl.setValue('');
    this.diseaseForm.controls.thumbnailUrl.markAsDirty();
  }

  statusLabel(status: DiseaseReviewStatus): string {
    return {
      DRAFT: 'Bản nháp',
      PENDING_REVIEW: 'Chờ duyệt',
      APPROVED: 'Đã duyệt',
      HIDDEN: 'Đã ẩn',
      REJECTED: 'Bị từ chối',
    }[status];
  }

  cropTypeLabel(cropTypeId: string): string {
    const cropType = this.cropTypes.find((item) => item.id === cropTypeId);
    return cropType ? `${cropType.name} (${cropType.code})` : cropTypeId;
  }

  toggleReviewHistory(disease: DiseaseView): void {
    if (this.expandedHistoryId === disease.id) {
      this.expandedHistoryId = null;
      return;
    }
    this.expandedHistoryId = disease.id;
    if (!this.reviewHistories[disease.id]) {
      this.loadReviewHistory(disease.id);
    }
  }

  reviewActionLabel(action: DiseaseReviewAction): string {
    return {
      CREATED: 'Tạo hồ sơ bệnh',
      SUBMITTED: 'Gửi duyệt',
      APPROVED: 'Phê duyệt',
      REJECTED: 'Từ chối',
      HIDDEN: 'Ẩn khỏi công khai',
      RESTORED: 'Khôi phục',
    }[action];
  }

  actorLabel(actorType: DiseaseReviewHistoryView['actorType']): string {
    return {
      ADMIN: 'Quản trị viên',
      BRAND: 'Thương hiệu',
      SYSTEM: 'Hệ thống',
    }[actorType];
  }

  loadRecentHistories(): void {
    if (!this.adminMode) return;
    this.recentHistoryRequest?.unsubscribe();
    this.recentHistoriesLoading = true;
    this.recentHistoriesError = '';
    this.recentHistoryRequest = this.catalogApi
      .listDiseaseReviewHistories({ page: 0, size: 5 })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.recentHistoriesLoading = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const page = unwrapApiResult<PageView<DiseaseReviewHistoryListItemView>>(response);
            if (!this.isHistoryPage(page)) {
              this.recentHistories = [];
              this.recentHistoriesError =
                'Máy chủ trả về dữ liệu lịch sử duyệt không hợp lệ.';
              return;
            }
            this.recentHistories = page.items.slice(0, 5);
          });
        },
        error: (error) => {
          this.syncView(() => {
            this.recentHistories = [];
            this.recentHistoriesError = apiErrorMessage(
              error,
              'Không thể tải lịch sử duyệt gần đây. Vui lòng thử lại.',
            );
          });
        },
      });
  }

  openHistoryExplorer(): void {
    if (!this.adminMode) return;
    this.historyExplorerOpen = true;
    this.historyError = '';
    this.loadHistoryPage(0);
    this.syncView();
    const dialog = this.historyDialog?.nativeElement;
    if (dialog && !dialog.open) dialog.showModal();
  }

  closeHistoryExplorer(): void {
    this.historyPageRequest?.unsubscribe();
    this.historyLoading = false;
    this.historyExplorerOpen = false;
    const dialog = this.historyDialog?.nativeElement;
    if (dialog?.open) dialog.close();
  }

  onHistoryDialogClosed(): void {
    this.historyPageRequest?.unsubscribe();
    this.historyLoading = false;
    this.historyExplorerOpen = false;
  }

  onHistoryKeywordInput(value: string): void {
    this.historyFilters.keyword = value;
    this.historyKeywordChanges.next(value.trim());
  }

  onHistoryFilterChange(): void {
    this.loadHistoryPage(0);
  }

  resetHistoryFilters(): void {
    this.historyFilters = this.emptyHistoryFilters();
    this.loadHistoryPage(0);
  }

  changeHistoryPage(delta: -1 | 1): void {
    if (!this.historyPage || this.historyLoading) return;
    const nextPage = this.historyPage.page + delta;
    if (nextPage < 0 || nextPage >= this.historyPage.totalPages) return;
    this.loadHistoryPage(nextPage);
  }

  loadHistoryPage(page: number): void {
    if (!this.adminMode || !this.historyExplorerOpen) return;
    this.historyPageRequest?.unsubscribe();
    this.historyLoading = true;
    this.historyError = '';
    this.historyPageRequest = this.catalogApi
      .listDiseaseReviewHistories(this.toHistoryApiFilters(page))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.historyLoading = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const result = unwrapApiResult<PageView<DiseaseReviewHistoryListItemView>>(
              response,
            );
            if (!this.isHistoryPage(result)) {
              this.historyPage = null;
              this.historyError = 'Máy chủ trả về dữ liệu lịch sử duyệt không hợp lệ.';
              return;
            }
            this.historyPage = result;
          });
        },
        error: (error) => {
          this.syncView(() => {
            this.historyPage = null;
            this.historyError = apiErrorMessage(
              error,
              'Không thể tải toàn bộ lịch sử duyệt. Vui lòng thử lại.',
            );
          });
        },
      });
  }

  private loadReviewHistory(diseaseId: string): void {
    this.historyLoadingId = diseaseId;
    this.catalogApi
      .listDiseaseReviewHistory(diseaseId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.historyLoadingId = null;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            this.reviewHistories[diseaseId] =
              unwrapApiResult<DiseaseReviewHistoryView[]>(response) ?? [];
          });
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải lịch sử xét duyệt bệnh.')),
      });
  }

  private runDiseaseAction(
    disease: DiseaseView,
    request: ReturnType<AgriCatalogApiService['submitDisease']>,
    successMessage: string,
    afterSuccess?: () => void,
  ): void {
    this.actionId = disease.id;
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.syncView(() => {
            this.actionId = null;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.syncView(() => {
            const updated = unwrapApiResult<DiseaseView>(response);
            if (updated) this.upsert(updated);
            delete this.reviewHistories[disease.id];
            if (this.expandedHistoryId === disease.id) {
              this.loadReviewHistory(disease.id);
            }
            afterSuccess?.();
            this.toast.success(successMessage);
            if (this.adminMode) {
              this.loadRecentHistories();
              if (this.historyExplorerOpen) this.loadHistoryPage(0);
            }
          });
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể thực hiện thao tác.')),
      });
  }

  private upsert(disease: DiseaseView): void {
    const exists = this.diseases.some((item) => item.id === disease.id);
    this.diseases = (
      exists
        ? this.diseases.map((item) => (item.id === disease.id ? disease : item))
        : [disease, ...this.diseases]
    ).sort(this.sortDiseases);
  }

  private emptyHistoryFilters(): HistoryExplorerFilters {
    return {
      keyword: '',
      action: '',
      newStatus: '',
      actorType: '',
      fromDate: '',
      toDate: '',
    };
  }

  private toHistoryApiFilters(page: number): DiseaseReviewHistoryFilters {
    return {
      keyword: this.historyFilters.keyword,
      action: this.historyFilters.action || null,
      newStatus: this.historyFilters.newStatus || null,
      actorType: this.historyFilters.actorType || null,
      from: this.toUtcPlus7Boundary(this.historyFilters.fromDate, false),
      to: this.toUtcPlus7Boundary(this.historyFilters.toDate, true),
      page,
      size: 20,
    };
  }

  private toUtcPlus7Boundary(value: string, exclusiveNextDay: boolean): string | null {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (!match) return null;
    const [, year, month, day] = match;
    const utcMilliseconds =
      Date.UTC(Number(year), Number(month) - 1, Number(day) + (exclusiveNextDay ? 1 : 0)) -
      7 * 60 * 60 * 1000;
    return new Date(utcMilliseconds).toISOString();
  }

  private isHistoryPage(
    value: PageView<DiseaseReviewHistoryListItemView> | null,
  ): value is PageView<DiseaseReviewHistoryListItemView> {
    return (
      value !== null &&
      Array.isArray(value.items) &&
      Number.isInteger(value.page) &&
      Number.isInteger(value.size) &&
      typeof value.totalElements === 'number' &&
      Number.isInteger(value.totalPages) &&
      typeof value.hasNext === 'boolean'
    );
  }

  private readonly sortDiseases = (left: DiseaseView, right: DiseaseView): number =>
    (right.updatedAt ?? right.createdAt ?? '').localeCompare(
      left.updatedAt ?? left.createdAt ?? '',
    );

  private syncView(update?: () => void): void {
    if (this.destroyRef.destroyed) return;
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  private buildPayload(): DiseasePayload {
    const value = this.diseaseForm.getRawValue();
    return {
      name: value.name.trim(),
      slug: value.slug.trim(),
      scientificName: this.nullIfBlank(value.scientificName),
      cropTypeId: value.cropTypeId,
      affectedPart: this.nullIfBlank(value.affectedPart),
      pathogenType: this.nullIfBlank(value.pathogenType),
      shortDescription: this.nullIfBlank(value.shortDescription),
      description: this.nullIfBlank(value.description),
      symptoms: this.nullIfBlank(value.symptoms),
      causes: this.nullIfBlank(value.causes),
      favorableConditions: this.nullIfBlank(value.favorableConditions),
      preventionMethod: this.nullIfBlank(value.preventionMethod),
      treatmentGuideline: this.nullIfBlank(value.treatmentGuideline),
      thumbnailUrl: this.nullIfBlank(value.thumbnailUrl),
    };
  }

  private nullIfBlank(value: string): string | null {
    const trimmed = value.trim();
    return trimmed || null;
  }

  private toSlug(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }
}
