import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, NgZone, afterNextRender, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  AgriCatalogApiService,
  ProductCategoryCreationPayload,
  ProductCategoryUpdatePayload,
  ProductCategoryView,
} from '../../../core/api/agri-catalog-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-catalog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './catalog.html',
  styleUrl: './catalog.scss',
})
export class AdminCatalog {
  private readonly fb = inject(FormBuilder);
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);

  categories: ProductCategoryView[] = [];
  loading = true;
  saving = false;
  loadError = '';
  editingCategoryId: string | null = null;
  deletingCategoryId: string | null = null;
  deleteConfirmationId: string | null = null;
  editorOpen = false;

  readonly categoryForm = this.fb.nonNullable.group({
    parentId: [''],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    slug: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.maxLength(10000)]],
    displayOrder: [0, [Validators.required, Validators.min(0)]],
    active: [true, Validators.required],
  });

  get editingCategory(): ProductCategoryView | null {
    return this.categories.find((category) => category.id === this.editingCategoryId) ?? null;
  }

  get parentOptions(): ProductCategoryView[] {
    return this.categories.filter((category) => this.canUseAsParent(category));
  }

  get activeCategoryCount(): number {
    return this.categories.filter((category) => category.active).length;
  }

  get inactiveCategoryCount(): number {
    return this.categories.length - this.activeCategoryCount;
  }

  constructor() {
    afterNextRender(() => this.loadCategories());
  }

  loadCategories(): void {
    this.loading = true;
    this.loadError = '';

    this.catalogApi
      .listCategories()
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const categories = unwrapApiResult<ProductCategoryView[]>(response);
          if (categories === null) {
            this.categories = [];
            this.loadError = apiResponseErrorMessage(
              typeof response === 'object' && response !== null && !Array.isArray(response)
                ? (response as { message?: string })
                : null,
              'Máy chủ trả về dữ liệu danh mục không hợp lệ.',
            );
            return;
          }

          this.categories = [...categories].sort(this.sortCategories);
        },
        error: (error) => {
          this.categories = [];
          this.loadError = apiErrorMessage(
            error,
            'Không thể tải danh sách danh mục. Vui lòng thử lại.',
          );
        },
      });
  }

  submitCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const value = this.categoryForm.getRawValue();
    const payload: ProductCategoryCreationPayload = {
      parentId: this.nullIfBlank(value.parentId),
      name: value.name.trim(),
      slug: value.slug.trim(),
      description: this.nullIfBlank(value.description),
      displayOrder: Number(value.displayOrder),
    };

    if (this.editingCategoryId) {
      this.updateCategory(this.editingCategoryId, {
        ...payload,
        active: value.active,
      });
      return;
    }

    this.createCategory(payload);
  }

  openCreate(): void {
    this.editingCategoryId = null;
    this.deleteConfirmationId = null;
    this.resetForm();
    this.editorOpen = true;
  }

  startEdit(category: ProductCategoryView): void {
    this.editingCategoryId = category.id;
    this.deleteConfirmationId = null;
    this.categoryForm.reset({
      parentId: category.parentId ?? '',
      name: category.name,
      slug: category.slug,
      description: category.description ?? '',
      displayOrder: category.displayOrder,
      active: category.active,
    });
    this.categoryForm.controls.slug.markAsDirty();
    this.editorOpen = true;
  }

  cancelEdit(): void {
    this.editorOpen = false;
    this.editingCategoryId = null;
    this.resetForm();
  }

  closeEditorFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget && !this.saving) {
      this.cancelEdit();
    }
  }

  requestDelete(categoryId: string): void {
    this.deleteConfirmationId = categoryId;
  }

  cancelDelete(): void {
    this.deleteConfirmationId = null;
  }

  deleteCategory(category: ProductCategoryView): void {
    if (this.deletingCategoryId) {
      return;
    }

    this.deletingCategoryId = category.id;
    this.catalogApi
      .deleteCategory(category.id)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.deletingCategoryId = null;
          });
        }),
      )
      .subscribe({
        next: () => {
          this.categories = this.categories.filter((candidate) => candidate.id !== category.id);
          this.deleteConfirmationId = null;
          if (this.editingCategoryId === category.id) {
            this.cancelEdit();
          }
          this.toast.success(`Đã xoá danh mục “${category.name}”.`);
        },
        error: (error) => {
          this.toast.error(apiErrorMessage(error, 'Không thể xoá danh mục.'));
        },
      });
  }

  private createCategory(payload: ProductCategoryCreationPayload): void {
    this.saving = true;
    this.catalogApi
      .createCategory(payload)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.saving = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const category = unwrapApiResult<ProductCategoryView>(response);
          if (!category) {
            this.toast.error('Máy chủ không trả về danh mục vừa tạo.');
            return;
          }

          this.categories = [...this.categories, category].sort(this.sortCategories);
          this.editorOpen = false;
          this.resetForm();
          this.toast.success(`Đã tạo danh mục “${category.name}”.`);
        },
        error: (error) => {
          this.toast.error(apiErrorMessage(error, 'Không thể tạo danh mục.'));
        },
      });
  }

  private updateCategory(categoryId: string, payload: ProductCategoryUpdatePayload): void {
    this.saving = true;
    this.catalogApi
      .updateCategory(categoryId, payload)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.saving = false;
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const updatedCategory = unwrapApiResult<ProductCategoryView>(response);
          if (!updatedCategory) {
            this.toast.error('Máy chủ không trả về danh mục vừa cập nhật.');
            return;
          }

          this.categories = this.categories
            .map((category) => (category.id === updatedCategory.id ? updatedCategory : category))
            .sort(this.sortCategories);
          this.editorOpen = false;
          this.editingCategoryId = null;
          this.resetForm();
          this.toast.success(`Đã cập nhật danh mục “${updatedCategory.name}”.`);
        },
        error: (error) => {
          this.toast.error(apiErrorMessage(error, 'Không thể cập nhật danh mục.'));
        },
      });
  }

  onCategoryNameInput(): void {
    const slugControl = this.categoryForm.controls.slug;
    if (!slugControl.dirty) {
      slugControl.setValue(this.toSlug(this.categoryForm.controls.name.value));
    }
  }

  parentName(category: ProductCategoryView): string {
    if (!category.parentId) {
      return 'Danh mục gốc';
    }
    return (
      this.categories.find((candidate) => candidate.id === category.parentId)?.name ??
      'Không xác định'
    );
  }

  private canUseAsParent(candidate: ProductCategoryView): boolean {
    if (!this.editingCategoryId) {
      return true;
    }

    let current: ProductCategoryView | undefined = candidate;
    const visited = new Set<string>();
    while (current && !visited.has(current.id)) {
      if (current.id === this.editingCategoryId) {
        return false;
      }
      visited.add(current.id);
      current = current.parentId
        ? this.categories.find((category) => category.id === current?.parentId)
        : undefined;
    }
    return true;
  }

  private resetForm(): void {
    this.categoryForm.reset({
      parentId: '',
      name: '',
      slug: '',
      description: '',
      displayOrder: 0,
      active: true,
    });
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  private nullIfBlank(value: string): string | null {
    const trimmed = value.trim();
    return trimmed ? trimmed : null;
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

  private readonly sortCategories = (
    left: ProductCategoryView,
    right: ProductCategoryView,
  ): number => left.displayOrder - right.displayOrder || left.name.localeCompare(right.name, 'vi');
}
