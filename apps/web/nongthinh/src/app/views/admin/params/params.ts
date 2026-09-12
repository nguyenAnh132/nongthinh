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
  imports: [CommonModule, FormsModule],
  templateUrl: './params.html',
  styleUrl: './params.scss',
})
export class Params {
  @Input() embedded = false;

  private readonly systemParamApi = inject(SystemParamApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);

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
    const changedParams = this.paramGroups
      .flatMap((group) => group.params)
      .filter((param) => this.serializeValue(param) !== param.originalValue);

    if (changedParams.length === 0) {
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
    return param.editValue ?? '';
  }
}
