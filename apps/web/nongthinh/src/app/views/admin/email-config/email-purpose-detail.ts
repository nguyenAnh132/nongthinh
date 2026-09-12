import { CommonModule } from '@angular/common';
import { afterNextRender, ChangeDetectorRef, Component, inject, NgZone } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';
import {
  EmailTemplatePurposeView,
  EmailTemplateView,
  EmailTemplateVariableView,
  NotificationApiService,
} from '../../../core/api/notification-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  EmailPurposeCard,
  EmailPurposeCode,
  EmailTemplateItem,
  EmailTemplateVariable,
  getPurposeByCode,
  getTemplatesByPurpose,
  getVariablesByPurpose,
} from './email-config.data';

@Component({
  selector: 'app-email-purpose-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './email-purpose-detail.html',
  styleUrl: './email-purpose-detail.scss',
})
export class EmailPurposeDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notificationApi = inject(NotificationApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);

  purpose: EmailPurposeCard | undefined;
  purposeId: number | null = null;
  variables: EmailTemplateVariable[] = [];
  templates: EmailTemplateItem[] = [];

  loading = true;
  activatingId: string | null = null;

  constructor() {
    afterNextRender(() => {
      const purposeCode = this.route.snapshot.paramMap.get('purposeCode') ?? '';
      this.purpose = getPurposeByCode(purposeCode);

      if (!this.purpose) {
        void this.router.navigate(['/admin/email-config']);
        return;
      }

      this.loadPurposeData(purposeCode);
    });
  }

  close(): void {
    void this.router.navigate(['/admin/email-config']);
  }

  toggleTemplateActive(template: EmailTemplateItem): void {
    if (template.isActive || !this.purposeId) {
      return;
    }

    this.activatingId = template.id;
    this.notificationApi.activateEmailTemplate(Number(template.id)).subscribe({
      next: () => {
        this.activatingId = null;
        this.templates = this.templates.map((item) => ({
          ...item,
          isActive: item.id === template.id,
        }));
        this.syncView();
      },
      error: (err) => {
        this.activatingId = null;
        this.toast.error(apiErrorMessage(err, 'Không thể kích hoạt mẫu email.'));
        this.syncView();
      },
    });
  }

  editTemplate(template: EmailTemplateItem): void {
    void this.router.navigate([
      '/admin/email-config',
      this.purpose?.code,
      'templates',
      template.id,
    ]);
  }

  openTemplate(template: EmailTemplateItem): void {
    this.editTemplate(template);
  }

  addTemplate(): void {
    void this.router.navigate(['/admin/email-config', this.purpose?.code, 'templates', 'new']);
  }

  previewTemplate(template: EmailTemplateItem): void {
    this.openTemplate(template);
  }

  private loadPurposeData(purposeCode: string): void {
    this.loading = true;

    this.notificationApi
      .getEmailTemplatePurposes()
      .pipe(
        switchMap((res) => {
          const purposes = unwrapApiResult<EmailTemplatePurposeView[]>(res) ?? [];
          const apiPurpose = purposes.find((item) => item.code === purposeCode);

          if (!apiPurpose) {
            throw new Error('PURPOSE_NOT_FOUND');
          }

          this.purposeId = apiPurpose.id;

          return forkJoin({
            templates: this.notificationApi.getEmailTemplatesByPurposeId(apiPurpose.id),
            variables: this.notificationApi.getEmailTemplateVariables(apiPurpose.id),
          });
        }),
        catchError(() => of(null)),
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: (result) => {
          if (!result) {
            this.applyFallbackData(purposeCode);
            return;
          }

          const templates = unwrapApiResult<EmailTemplateView[]>(result.templates) ?? [];
          const variables = unwrapApiResult<EmailTemplateVariableView[]>(result.variables) ?? [];

          this.templates = templates.map((template) => this.toTemplateItem(template));
          this.variables = variables.map((variable) => this.toTemplateVariable(variable));

          if (this.templates.length === 0 && this.variables.length === 0) {
            this.applyFallbackData(purposeCode);
          }
        },
        error: (err) => {
          this.applyFallbackData(purposeCode);
          this.toast.error(apiErrorMessage(err, 'Không thể tải cấu hình email.'));
        },
      });
  }

  private applyFallbackData(purposeCode: string): void {
    const code = purposeCode as EmailPurposeCode;
    this.variables = getVariablesByPurpose(code);
    this.templates = getTemplatesByPurpose(code);
    this.purposeId = null;
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  private toTemplateItem(template: EmailTemplateView): EmailTemplateItem {
    return {
      id: String(template.id),
      name: template.name,
      description: template.description,
      createdAt: new Date(template.createdAt).toLocaleDateString('vi-VN'),
      isActive: template.active,
    };
  }

  private toTemplateVariable(variable: EmailTemplateVariableView): EmailTemplateVariable {
    return {
      name: variable.variableName,
      description: variable.description,
      exampleValue: variable.exampleValue,
      required: variable.required,
    };
  }
}
