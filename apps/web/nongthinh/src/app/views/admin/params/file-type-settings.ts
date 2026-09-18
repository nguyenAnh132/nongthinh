import { Component, DestroyRef, afterNextRender, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { catchError, finalize, forkJoin, map, of } from 'rxjs';
import { FilePurpose } from '../../../core/api/file-api.service';
import {
  FILE_PURPOSE_LABELS, FILE_PURPOSES, FileConfigurationApiService, FileUploadConfiguration,
} from '../../../core/api/file-configuration-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { UploadPolicyService } from '../../../core/service/upload-policy.service';
import { ToastService } from '../../../shared/toast/toast.service';

interface EditablePolicy extends FileUploadConfiguration {
  original: Record<string, boolean>;
}

@Component({
  selector: 'app-file-type-settings',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './file-type-settings.html',
  styleUrl: './file-type-settings.scss',
})
export class FileTypeSettings {
  private readonly api = inject(FileConfigurationApiService);
  private readonly policies = inject(UploadPolicyService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  readonly labels = FILE_PURPOSE_LABELS;
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly configurations = signal<EditablePolicy[]>([]);

  get canWrite(): boolean { return this.auth.hasPermission('system:config:write'); }
  get changedCount(): number {
    return this.configurations().reduce((count, policy) =>
      count + policy.fileTypes.filter((type) => type.enabled !== policy.original[type.code]).length, 0);
  }

  constructor() { afterNextRender(() => this.load()); }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    forkJoin(FILE_PURPOSES.map((purpose) => this.api.getConfiguration(purpose).pipe(map((response) => {
      const policy = unwrapApiResult<FileUploadConfiguration>(response);
      if (!policy || !Array.isArray(policy.fileTypes)) throw new Error('Cấu hình định dạng chưa đầy đủ.');
      return { ...policy, original: Object.fromEntries(policy.fileTypes.map((type) => [type.code, type.enabled])) };
    })))).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (policies) => this.configurations.set(policies),
      error: (error) => this.error.set(apiErrorMessage(error, 'Không thể tải cấu hình định dạng tệp.')),
    });
  }

  setEnabled(purpose: FilePurpose, code: string, enabled: boolean): void {
    if (!this.canWrite || this.saving()) return;
    this.configurations.update((policies) => policies.map((policy) => policy.purpose !== purpose ? policy : {
      ...policy, fileTypes: policy.fileTypes.map((type) => type.code === code ? { ...type, enabled } : type),
    }));
  }

  enabledCount(policy: EditablePolicy): number {
    return policy.fileTypes.filter((type) => type.enabled).length;
  }

  save(): void {
    if (!this.canWrite || this.saving()) return;
    const changes = this.configurations().flatMap((policy) => policy.fileTypes
      .filter((type) => type.enabled !== policy.original[type.code])
      .map((type) => ({ purpose: policy.purpose, code: type.code, enabled: type.enabled })));
    if (!changes.length) return;
    this.saving.set(true);
    this.error.set('');
    forkJoin(changes.map((change) => this.api.updateFileType(change.purpose, change.code, change.enabled).pipe(
      map((response) => {
        const updated = unwrapApiResult<{ enabled: boolean }>(response);
        if (!updated || updated.enabled !== change.enabled) throw new Error('Không thể lưu định dạng.');
        return { ...change, success: true };
      }),
      catchError(() => of({ ...change, success: false })),
    ))).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.saving.set(false)),
    ).subscribe((results) => {
      for (const result of results.filter((item) => item.success)) {
        this.policies.invalidate(result.purpose);
        this.configurations.update((policies) => policies.map((policy) => policy.purpose === result.purpose
          ? { ...policy, original: { ...policy.original, [result.code]: result.enabled } } : policy));
      }
      const failed = results.filter((item) => !item.success).length;
      if (failed) {
        this.error.set(`Còn ${failed} thay đổi chưa lưu được. Các thay đổi thành công đã được giữ lại; vui lòng thử lưu lại.`);
      } else {
        this.toast.success('Đã lưu định dạng tệp được chấp nhận.');
      }
    });
  }
}
