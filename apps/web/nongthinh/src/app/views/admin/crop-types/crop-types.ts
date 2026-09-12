import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, NgZone, afterNextRender, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeCreationPayload,
  CropTypeUpdatePayload,
  CropTypeView,
} from '../../../core/api/agri-catalog-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-crop-types',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crop-types.html',
  styleUrl: './crop-types.scss',
})
export class AdminCropTypes {
  private readonly formBuilder = inject(FormBuilder);
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);

  cropTypes: CropTypeView[] = [];
  searchTerm = '';
  loading = true;
  saving = false;
  loadError = '';
  editorOpen = false;
  editingCropTypeId: string | null = null;
  deletingCropTypeId: string | null = null;
  deleteConfirmationId: string | null = null;

  readonly cropTypeForm = this.formBuilder.nonNullable.group({
    code: [
      '',
      [
        Validators.required,
        Validators.maxLength(50),
        Validators.pattern(/^[A-Z][A-Z0-9_]*$/),
      ],
    ],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(10000)],
    active: [true, Validators.required],
  });

  get editingCropType(): CropTypeView | null {
    return this.cropTypes.find((cropType) => cropType.id === this.editingCropTypeId) ?? null;
  }

  get filteredCropTypes(): CropTypeView[] {
    const search = this.searchTerm.trim().toLocaleLowerCase('vi');
    if (!search) return this.cropTypes;
    return this.cropTypes.filter((cropType) =>
      [cropType.code, cropType.name, cropType.description].some((value) =>
        value?.toLocaleLowerCase('vi').includes(search),
      ),
    );
  }

  get activeCropTypeCount(): number {
    return this.cropTypes.filter((cropType) => cropType.active).length;
  }

  get inactiveCropTypeCount(): number {
    return this.cropTypes.length - this.activeCropTypeCount;
  }

  constructor() {
    afterNextRender(() => this.loadCropTypes());
  }

  loadCropTypes(): void {
    this.loading = true;
    this.loadError = '';
    this.catalogApi
      .listCropTypes()
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const cropTypes = unwrapApiResult<CropTypeView[]>(response);
          if (!Array.isArray(cropTypes)) {
            this.cropTypes = [];
            this.loadError = apiResponseErrorMessage(
              typeof response === 'object' && response !== null && !Array.isArray(response)
                ? (response as { message?: string })
                : null,
              'Máy chủ trả về dữ liệu loại cây trồng không hợp lệ.',
            );
            return;
          }
          this.cropTypes = [...cropTypes].sort(this.sortCropTypes);
        },
        error: (error) => {
          this.cropTypes = [];
          this.loadError = apiErrorMessage(
            error,
            'Không thể tải danh sách loại cây trồng. Vui lòng thử lại.',
          );
        },
      });
  }

  openCreate(): void {
    this.editingCropTypeId = null;
    this.deleteConfirmationId = null;
    this.resetForm();
    this.editorOpen = true;
  }

  startEdit(cropType: CropTypeView): void {
    this.editingCropTypeId = cropType.id;
    this.deleteConfirmationId = null;
    this.cropTypeForm.reset({
      code: cropType.code,
      name: cropType.name,
      description: cropType.description ?? '',
      active: cropType.active,
    });
    this.editorOpen = true;
  }

  cancelEdit(): void {
    if (this.saving) return;
    this.editorOpen = false;
    this.editingCropTypeId = null;
    this.resetForm();
  }

  closeEditorFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.cancelEdit();
  }

  submitCropType(): void {
    if (this.cropTypeForm.invalid) {
      this.cropTypeForm.markAllAsTouched();
      return;
    }

    const value = this.cropTypeForm.getRawValue();
    if (this.editingCropTypeId) {
      this.updateCropType(this.editingCropTypeId, {
        name: value.name.trim(),
        description: this.nullIfBlank(value.description),
        active: value.active,
      });
      return;
    }

    this.createCropType({
      code: value.code.trim().toUpperCase(),
      name: value.name.trim(),
      description: this.nullIfBlank(value.description),
    });
  }

  onCodeInput(): void {
    const codeControl = this.cropTypeForm.controls.code;
    const normalizedCode = codeControl.value
      .toUpperCase()
      .replace(/[^A-Z0-9_]/g, '_')
      .replace(/_+/g, '_');
    if (normalizedCode !== codeControl.value) codeControl.setValue(normalizedCode);
  }

  requestDelete(cropTypeId: string): void {
    this.deleteConfirmationId = cropTypeId;
  }

  cancelDelete(): void {
    this.deleteConfirmationId = null;
  }

  deleteCropType(cropType: CropTypeView): void {
    if (this.deletingCropTypeId) return;

    this.deletingCropTypeId = cropType.id;
    this.catalogApi
      .deleteCropType(cropType.id)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.deletingCropTypeId = null;
          });
        }),
      )
      .subscribe({
        next: () => {
          this.cropTypes = this.cropTypes.filter((candidate) => candidate.id !== cropType.id);
          this.deleteConfirmationId = null;
          if (this.editingCropTypeId === cropType.id) this.cancelEdit();
          this.toast.success(`Đã xóa loại cây trồng “${cropType.name}”.`);
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể xóa loại cây trồng.')),
      });
  }

  private createCropType(payload: CropTypeCreationPayload): void {
    this.saving = true;
    this.catalogApi
      .createCropType(payload)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.saving = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const cropType = unwrapApiResult<CropTypeView>(response);
          if (!cropType) {
            this.toast.error('Máy chủ không trả về loại cây trồng vừa tạo.');
            return;
          }
          this.cropTypes = [...this.cropTypes, cropType].sort(this.sortCropTypes);
          this.editorOpen = false;
          this.resetForm();
          this.toast.success(`Đã tạo loại cây trồng “${cropType.name}”.`);
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tạo loại cây trồng.')),
      });
  }

  private updateCropType(cropTypeId: string, payload: CropTypeUpdatePayload): void {
    this.saving = true;
    this.catalogApi
      .updateCropType(cropTypeId, payload)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.saving = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const updatedCropType = unwrapApiResult<CropTypeView>(response);
          if (!updatedCropType) {
            this.toast.error('Máy chủ không trả về loại cây trồng vừa cập nhật.');
            return;
          }
          this.cropTypes = this.cropTypes
            .map((cropType) => (cropType.id === updatedCropType.id ? updatedCropType : cropType))
            .sort(this.sortCropTypes);
          this.editorOpen = false;
          this.editingCropTypeId = null;
          this.resetForm();
          this.toast.success(`Đã cập nhật loại cây trồng “${updatedCropType.name}”.`);
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể cập nhật loại cây trồng.')),
      });
  }

  private resetForm(): void {
    this.cropTypeForm.reset({
      code: '',
      name: '',
      description: '',
      active: true,
    });
  }

  private nullIfBlank(value: string): string | null {
    const trimmedValue = value.trim();
    return trimmedValue || null;
  }

  private readonly sortCropTypes = (left: CropTypeView, right: CropTypeView): number =>
    left.name.localeCompare(right.name, 'vi') || left.code.localeCompare(right.code);

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.changeDetector.detectChanges();
    });
  }
}
