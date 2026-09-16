import { Component, afterNextRender, ChangeDetectorRef, inject, Input, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import {
  SystemParamApiService,
  SystemParamTypeGroupView,
  SystemParamView,
} from '../../../core/api/system-param-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import { FileTypeSettings } from './file-type-settings';
import { FILE_PURPOSE_LABELS } from '../../../core/api/file-configuration-api.service';
import { FilePurpose } from '../../../core/api/file-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UploadPolicyService, formatFileSize } from '../../../core/service/upload-policy.service';

export interface EditableSystemParam extends SystemParamView {
  editValue: string;
  editBoolean: boolean;
  originalValue: string;
}

export interface ParamGroupState extends SystemParamTypeGroupView {
  params: EditableSystemParam[];
}

@Component({
  selector: 'app-params',
  standalone: true,
  imports: [CommonModule, FormsModule, FileTypeSettings],
  templateUrl: './params.html',
  styleUrl: './params.scss',
})
export class Params {
  @Input() embedded = false;

  private readonly systemParamApi = inject(SystemParamApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly uploadPolicies = inject(UploadPolicyService);

  get canWrite(): boolean { return this.auth.hasPermission('system:config:write'); }

  filePurpose(param: SystemParamView): FilePurpose | null {
    if (!param.name.startsWith('FILE_UPLOAD_MAX_BYTES_')) return null;
    const purpose = param.name.replace(/^FILE_UPLOAD_MAX_BYTES_/, '') as FilePurpose;
    return purpose in FILE_PURPOSE_LABELS ? purpose : null;
  }

  paramLabel(param: SystemParamView): string {
    const purpose = this.filePurpose(param);
    return purpose ? FILE_PURPOSE_LABELS[purpose] + ' — kích thước tối đa (byte)' : param.name;
  }

  sizeHint(param: EditableSystemParam): string {
    const bytes = Number(param.editValue);
    return Number.isSafeInteger(bytes) && bytes > 0 ? formatFileSize(bytes) : 'Nhập số byte nguyên lớn hơn 0.';
  }

  isFileGroup(group: ParamGroupState): boolean {
    return group.name === 'Cấu hình tệp' || group.params.some((param) => this.filePurpose(param) !== null);
  }

  loading = true;
  saving = false;

  paramGroups: ParamGroupState[] = [];

  constructor() {
    afterNextRender(() => {
      this.loadParams();
    });
  }

  loadParams() {
    this.loading = true;

    this.systemParamApi
      .getGroupedSystemParamTypes()
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: (res) => {
          const groups = unwrapApiResult<SystemParamTypeGroupView[]>(res);
          if (groups === null) {
            this.toast.error(
              apiResponseErrorMessage(
                typeof res === 'object' && res !== null && !Array.isArray(res)
                  ? (res as { message?: string })
                  : null,
                'Không thể tải cấu hình hệ thống.',
              ),
            );
            this.paramGroups = [];
            return;
          }

          this.paramGroups = groups.map((group) => ({
            ...group,
            params: (group.params ?? []).map((param) => this.toEditableParam(param)),
          }));
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Không thể tải cấu hình hệ thống.'));
        },
      });
  }

  saveAll() {
    if (!this.canWrite || this.saving) return;
    const changedParams = this.paramGroups
      .flatMap((group) => group.params)
      .filter((param) => this.serializeValue(param) !== param.originalValue);

    if (changedParams.length === 0) {
      return;
    }

    if (changedParams.some((param) => this.filePurpose(param) && (
      !Number.isSafeInteger(Number(param.editValue)) || Number(param.editValue) <= 0 || Number(param.editValue) > 2147483647
    ))) {
      this.toast.error('Kích thước tệp phải là số byte nguyên từ 1 đến 2147483647.');
      return;
    }

    this.saving = true;

    forkJoin(
      changedParams.map((param) =>
        this.systemParamApi.updateSystemParam(param.name, {
          value: this.serializeValue(param),
          description: param.description,
        }),
      ),
    )
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.saving = false;
          });
        }),
      )
      .subscribe({
        next: () => {
          changedParams.forEach((param) => {
            const purpose = this.filePurpose(param);
            if (purpose) this.uploadPolicies.invalidate(purpose);
          });
          this.toast.success('Đã lưu cấu hình thành công.');
          this.loadParams();
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Không thể lưu cấu hình.'));
        },
      });
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  sectionId(group: ParamGroupState): string {
    return `param-group-${group.id}`;
  }

  isLongValue(param: EditableSystemParam): boolean {
    return (
      param.dataType === 'STRING' &&
      (param.name.includes('TEMPLATE') ||
        param.name.includes('ORIGINS') ||
        (param.editValue?.length ?? 0) > 80)
    );
  }

  scrollToSection(id: string, event: Event) {
    event.preventDefault();
    const element = document.getElementById(id);
    if (element) {
      const y = element.getBoundingClientRect().top + window.scrollY - 100;
      window.scrollTo({ top: y, behavior: 'smooth' });
    }
  }

  private toEditableParam(param: SystemParamView): EditableSystemParam {
    return {
      ...param,
      editValue: param.value,
      editBoolean: param.value === 'true' || param.value === '1',
      originalValue: param.value,
    };
  }

  private serializeValue(param: EditableSystemParam): string {
    if (param.dataType === 'BOOLEAN') {
      return param.editBoolean ? 'true' : 'false';
    }
    return String(param.editValue ?? '');
  }
}
