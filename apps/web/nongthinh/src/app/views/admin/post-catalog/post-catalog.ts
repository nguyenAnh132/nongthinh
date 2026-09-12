import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, NgZone, afterNextRender, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import {
  PostApiService,
  PostTopicView,
  PostTypeView,
} from '../../../core/api/post-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

type CatalogKind = 'type' | 'topic';

interface CatalogRow {
  id: string;
  identifier: string;
  name: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
}

@Component({
  selector: 'app-admin-post-catalog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './post-catalog.html',
  styleUrl: './post-catalog.scss',
})
export class AdminPostCatalog {
  private readonly formBuilder = inject(FormBuilder);
  private readonly postApi = inject(PostApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);

  postTypes: PostTypeView[] = [];
  postTopics: PostTopicView[] = [];
  activeKind: CatalogKind = 'type';
  searchTerm = '';
  loading = true;
  saving = false;
  loadError = '';
  editorOpen = false;
  editingId: string | null = null;
  deletingId: string | null = null;
  deleteConfirmationId: string | null = null;

  readonly catalogForm = this.formBuilder.nonNullable.group({
    identifier: ['', [Validators.required, Validators.maxLength(180)]],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(500)],
    displayOrder: [0, [Validators.required, Validators.min(0), Validators.max(1_000_000)]],
    active: [true, Validators.required],
  });

  constructor() {
    afterNextRender(() => this.loadCatalog());
  }

  get rows(): CatalogRow[] {
    const rows = this.activeKind === 'type'
      ? this.postTypes.map((item) => this.typeToRow(item))
      : this.postTopics.map((item) => this.topicToRow(item));
    const search = this.searchTerm.trim().toLocaleLowerCase('vi');
    if (!search) return rows;
    return rows.filter((row) =>
      [row.identifier, row.name, row.description].some((value) =>
        value?.toLocaleLowerCase('vi').includes(search),
      ),
    );
  }

  get totalCount(): number {
    return this.activeKind === 'type' ? this.postTypes.length : this.postTopics.length;
  }

  get activeCount(): number {
    return (this.activeKind === 'type' ? this.postTypes : this.postTopics).filter(
      (item) => item.active,
    ).length;
  }

  get editingRow(): CatalogRow | null {
    return this.rows.find((row) => row.id === this.editingId) ?? null;
  }

  get entityLabel(): string {
    return this.activeKind === 'type' ? 'loại bài viết' : 'chủ đề';
  }

  get identifierLabel(): string {
    return this.activeKind === 'type' ? 'Mã loại bài' : 'Slug chủ đề';
  }

  loadCatalog(): void {
    this.loading = true;
    this.loadError = '';
    forkJoin({ types: this.postApi.listPostTypes(), topics: this.postApi.listPostTopics() })
      .pipe(finalize(() => this.syncView(() => (this.loading = false))))
      .subscribe({
        next: ({ types, topics }) => {
          const postTypes = unwrapApiResult<PostTypeView[]>(types);
          const postTopics = unwrapApiResult<PostTopicView[]>(topics);
          if (!Array.isArray(postTypes) || !Array.isArray(postTopics)) {
            this.postTypes = [];
            this.postTopics = [];
            this.loadError = apiResponseErrorMessage(
              !Array.isArray(postTypes) ? types : topics,
              'Máy chủ trả về dữ liệu danh mục bài viết không hợp lệ.',
            );
            return;
          }
          this.postTypes = [...postTypes].sort(this.sortItems);
          this.postTopics = [...postTopics].sort(this.sortItems);
        },
        error: (error) => {
          this.postTypes = [];
          this.postTopics = [];
          this.loadError = apiErrorMessage(
            error,
            'Không thể tải danh mục bài viết. Vui lòng thử lại.',
          );
        },
      });
  }

  selectKind(kind: CatalogKind): void {
    if (this.saving) return;
    this.activeKind = kind;
    this.searchTerm = '';
    this.closeEditor();
  }

  openCreate(): void {
    this.editingId = null;
    this.deleteConfirmationId = null;
    this.resetForm();
    this.editorOpen = true;
  }

  startEdit(row: CatalogRow): void {
    this.editingId = row.id;
    this.deleteConfirmationId = null;
    this.catalogForm.reset({
      identifier: row.identifier,
      name: row.name,
      description: row.description ?? '',
      displayOrder: row.displayOrder,
      active: row.active,
    });
    this.editorOpen = true;
  }

  closeEditor(): void {
    if (this.saving) return;
    this.editorOpen = false;
    this.editingId = null;
    this.resetForm();
  }

  closeEditorFromBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closeEditor();
  }

  normalizeIdentifier(): void {
    const control = this.catalogForm.controls.identifier;
    const normalized = this.activeKind === 'type'
      ? control.value.toUpperCase().replace(/[^A-Z0-9_]/g, '_').replace(/_+/g, '_')
      : control.value
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .replace(/đ/g, 'd')
          .replace(/Đ/g, 'd')
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/^-+|-+$/g, '');
    if (normalized !== control.value) control.setValue(normalized);
  }

  submit(): void {
    if (this.catalogForm.invalid) {
      this.catalogForm.markAllAsTouched();
      return;
    }
    const value = this.catalogForm.getRawValue();
    const common = {
      name: value.name.trim(),
      description: this.nullIfBlank(value.description),
      displayOrder: value.displayOrder,
    };

    if (this.activeKind === 'type') {
      if (this.editingId) {
        this.updateType(this.editingId, { ...common, active: value.active });
      } else {
        this.createType({ ...common, code: value.identifier });
      }
    } else if (this.editingId) {
      this.updateTopic(this.editingId, { ...common, active: value.active });
    } else {
      this.createTopic({ ...common, slug: value.identifier });
    }
  }

  requestDelete(id: string): void {
    this.deleteConfirmationId = id;
  }

  cancelDelete(): void {
    this.deleteConfirmationId = null;
  }

  delete(row: CatalogRow): void {
    if (this.deletingId) return;
    this.deletingId = row.id;
    const request = this.activeKind === 'type'
      ? this.postApi.deletePostType(row.id)
      : this.postApi.deletePostTopic(row.id);
    request.pipe(finalize(() => this.syncView(() => (this.deletingId = null)))).subscribe({
      next: () => {
        if (this.activeKind === 'type') {
          this.postTypes = this.postTypes.filter((item) => item.id !== row.id);
        } else {
          this.postTopics = this.postTopics.filter((item) => item.id !== row.id);
        }
        this.deleteConfirmationId = null;
        if (this.editingId === row.id) this.closeEditor();
        this.toast.success(`Đã xóa ${this.entityLabel} “${row.name}”.`);
      },
      error: (error) =>
        this.toast.error(apiErrorMessage(error, `Không thể xóa ${this.entityLabel}.`)),
    });
  }

  private createType(payload: Parameters<PostApiService['createPostType']>[0]): void {
    this.saving = true;
    this.postApi.createPostType(payload)
      .pipe(finalize(() => this.syncView(() => (this.saving = false))))
      .subscribe({
        next: (response) => {
          const item = unwrapApiResult<PostTypeView>(response);
          if (!item) return this.toast.error('Máy chủ không trả về loại bài viết vừa tạo.');
          this.postTypes = [...this.postTypes, item].sort(this.sortItems);
          this.finishSave(item.name, 'tạo');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể tạo loại bài viết.')),
      });
  }

  private updateType(id: string, payload: Parameters<PostApiService['updatePostType']>[1]): void {
    this.saving = true;
    this.postApi.updatePostType(id, payload)
      .pipe(finalize(() => this.syncView(() => (this.saving = false))))
      .subscribe({
        next: (response) => {
          const item = unwrapApiResult<PostTypeView>(response);
          if (!item) return this.toast.error('Máy chủ không trả về loại bài viết vừa cập nhật.');
          this.postTypes = this.postTypes.map((current) => current.id === item.id ? item : current).sort(this.sortItems);
          this.finishSave(item.name, 'cập nhật');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể cập nhật loại bài viết.')),
      });
  }

  private createTopic(payload: Parameters<PostApiService['createPostTopic']>[0]): void {
    this.saving = true;
    this.postApi.createPostTopic(payload)
      .pipe(finalize(() => this.syncView(() => (this.saving = false))))
      .subscribe({
        next: (response) => {
          const item = unwrapApiResult<PostTopicView>(response);
          if (!item) return this.toast.error('Máy chủ không trả về chủ đề vừa tạo.');
          this.postTopics = [...this.postTopics, item].sort(this.sortItems);
          this.finishSave(item.name, 'tạo');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể tạo chủ đề.')),
      });
  }

  private updateTopic(id: string, payload: Parameters<PostApiService['updatePostTopic']>[1]): void {
    this.saving = true;
    this.postApi.updatePostTopic(id, payload)
      .pipe(finalize(() => this.syncView(() => (this.saving = false))))
      .subscribe({
        next: (response) => {
          const item = unwrapApiResult<PostTopicView>(response);
          if (!item) return this.toast.error('Máy chủ không trả về chủ đề vừa cập nhật.');
          this.postTopics = this.postTopics.map((current) => current.id === item.id ? item : current).sort(this.sortItems);
          this.finishSave(item.name, 'cập nhật');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể cập nhật chủ đề.')),
      });
  }

  private finishSave(name: string, action: string): void {
    this.editorOpen = false;
    this.editingId = null;
    this.resetForm();
    this.toast.success(`Đã ${action} ${this.entityLabel} “${name}”.`);
  }

  private resetForm(): void {
    this.catalogForm.reset({ identifier: '', name: '', description: '', displayOrder: 0, active: true });
  }

  private typeToRow(item: PostTypeView): CatalogRow {
    return { ...item, identifier: item.code };
  }

  private topicToRow(item: PostTopicView): CatalogRow {
    return { ...item, identifier: item.slug };
  }

  private nullIfBlank(value: string): string | null {
    return value.trim() || null;
  }

  private readonly sortItems = <T extends { displayOrder: number; name: string }>(left: T, right: T): number =>
    left.displayOrder - right.displayOrder || left.name.localeCompare(right.name, 'vi');

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.changeDetector.detectChanges();
    });
  }
}
