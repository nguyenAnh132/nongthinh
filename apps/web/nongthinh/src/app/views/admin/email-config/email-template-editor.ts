import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import {
  EmailTemplatePurposeView,
  EmailTemplateView,
  NotificationApiService,
  EmailTemplateVariableView,
  PreviewEmailTemplateView,
} from '../../../core/api/notification-api.service';
import { apiErrorMessage, apiResponseErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  EmailPurposeCard,
  EmailTemplateDetail,
  EmailTemplateVariable,
  createEmptyTemplateDetail,
  getPurposeByCode,
} from './email-config.data';

type PreviewTab = 'html' | 'text';

@Component({
  selector: 'app-email-template-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './email-template-editor.html',
  styleUrl: './email-template-editor.scss',
})
export class EmailTemplateEditor implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly notificationApi = inject(NotificationApiService);
  private readonly toast = inject(ToastService);

  purpose: EmailPurposeCard | undefined;
  purposeId: number | null = null;
  variables: EmailTemplateVariable[] = [];
  template: EmailTemplateDetail | undefined;
  isCreateMode = false;

  name = '';
  description = '';
  subject = '';
  htmlContent = '';
  textContent = '';

  previewTab: PreviewTab = 'html';
  previewHtml: SafeHtml = '';
  previewText = '';
  saving = false;
  loading = true;

  ngOnInit(): void {
    const purposeCode = this.route.snapshot.paramMap.get('purposeCode') ?? '';
    const templateId = this.route.snapshot.paramMap.get('templateId');
    const lastPathSegment = this.route.snapshot.url.at(-1)?.path ?? '';

    this.purpose = getPurposeByCode(purposeCode);

    if (!this.purpose) {
      void this.router.navigate(['/admin/email-config']);
      return;
    }

    this.isCreateMode = templateId === 'new' || lastPathSegment === 'new';

    this.notificationApi.getEmailTemplatePurposes().subscribe({
      next: (res) => {
        const purposes = unwrapApiResult<EmailTemplatePurposeView[]>(res) ?? [];
        const apiPurpose = purposes.find((item) => item.code === purposeCode);
        if (!apiPurpose) {
          this.toast.error(apiResponseErrorMessage(res, 'Không tìm thấy mục đích email.'));
          this.loading = false;
          return;
        }

        this.purposeId = apiPurpose.id;
        this.loadVariables(apiPurpose.id);

        if (this.isCreateMode) {
          this.template = createEmptyTemplateDetail();
          this.bindTemplateFields();
          this.loading = false;
          this.refreshPreview();
          return;
        }

        if (!templateId) {
          void this.router.navigate(['/admin/email-config', this.purpose?.code]);
          return;
        }

        this.loadTemplate(Number(templateId));
      },
      error: (err) => {
        this.toast.error(apiErrorMessage(err, 'Không thể tải cấu hình email.'));
        this.loading = false;
      },
    });
  }

  back(): void {
    void this.router.navigate(['/admin/email-config', this.purpose?.code]);
  }

  setPreviewTab(tab: PreviewTab): void {
    this.previewTab = tab;
  }

  refreshPreview(): void {
    if (!this.purposeId) {
      this.applyLocalPreview();
      return;
    }

    const payload = {
      subject: this.subject,
      htmlContent: this.htmlContent,
      textContent: this.textContent,
    };

    if (this.isCreateMode) {
      this.notificationApi.previewDraftEmailTemplate(this.purposeId, payload).subscribe({
        next: (res) => {
          const preview = unwrapApiResult<PreviewEmailTemplateView>(res);
          if (!preview) {
            this.applyLocalPreview();
            return;
          }
          this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(preview.htmlContent);
          this.previewText = preview.textContent;
        },
        error: () => {
          this.applyLocalPreview();
        },
      });
      return;
    }

    if (!this.template || this.template.id === 'new') {
      this.applyLocalPreview();
      return;
    }

    this.notificationApi.previewDraftEmailTemplate(this.purposeId, payload).subscribe({
      next: (res) => {
        const preview = unwrapApiResult<PreviewEmailTemplateView>(res);
        if (!preview) {
          this.applyLocalPreview();
          return;
        }
        this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(preview.htmlContent);
        this.previewText = preview.textContent;
      },
      error: () => {
        this.applyLocalPreview();
      },
    });
  }

  save(): void {
    if (!this.purposeId) {
      return;
    }

    this.saving = true;

    const payload = {
      name: this.name,
      description: this.description,
      subject: this.subject,
      htmlContent: this.htmlContent,
      textContent: this.textContent,
    };

    if (this.isCreateMode) {
      this.notificationApi.createEmailTemplate(this.purposeId, payload).subscribe({
        next: () => {
          this.saving = false;
          this.toast.success('Đã tạo mẫu email.');
          void this.router.navigate(['/admin/email-config', this.purpose?.code]);
        },
        error: (err) => {
          this.saving = false;
          this.toast.error(apiErrorMessage(err, 'Không thể tạo mẫu email.'));
        },
      });
      return;
    }

    if (!this.template || this.template.id === 'new') {
      this.saving = false;
      return;
    }

    this.notificationApi.updateEmailTemplate(Number(this.template.id), payload).subscribe({
      next: (res) => {
        this.saving = false;
        const updated = unwrapApiResult<EmailTemplateView>(res);
        if (updated) {
          this.template = {
            id: String(updated.id),
            name: updated.name,
            description: updated.description,
            createdAt: new Date(updated.createdAt).toLocaleDateString('vi-VN'),
            isActive: updated.active,
            subject: updated.subject,
            htmlContent: updated.htmlContent,
            textContent: updated.textContent,
          };
        }
        this.toast.success('Đã lưu mẫu email.');
        this.refreshPreview();
      },
      error: (err) => {
        this.saving = false;
        this.toast.error(apiErrorMessage(err, 'Không thể lưu mẫu email.'));
      },
    });
  }

  get pageTitle(): string {
    return this.isCreateMode ? 'Thêm mẫu mới' : 'Chỉnh sửa mẫu email';
  }

  get saveLabel(): string {
    if (this.saving) {
      return this.isCreateMode ? 'Đang tạo...' : 'Đang lưu...';
    }
    return this.isCreateMode ? 'Tạo mẫu' : 'Lưu thay đổi';
  }

  get statusLabel(): string {
    return this.template?.isActive ? 'Đang kích hoạt' : 'Chưa kích hoạt';
  }

  private loadVariables(purposeId: number): void {
    this.notificationApi.getEmailTemplateVariables(purposeId).subscribe({
      next: (res) => {
        const variables = unwrapApiResult<EmailTemplateVariableView[]>(res) ?? [];
        this.variables = variables.map((variable) => this.toTemplateVariable(variable));
      },
      error: () => {
        this.variables = [];
      },
    });
  }

  private loadTemplate(templateId: number): void {
    this.notificationApi.getEmailTemplate(templateId).subscribe({
      next: (res) => {
        const template = unwrapApiResult<EmailTemplateView>(res);
        if (!template) {
          this.toast.error(apiResponseErrorMessage(res, 'Không thể tải mẫu email.'));
          this.loading = false;
          return;
        }

        this.template = {
          id: String(template.id),
          name: template.name,
          description: template.description,
          createdAt: new Date(template.createdAt).toLocaleDateString('vi-VN'),
          isActive: template.active,
          subject: template.subject,
          htmlContent: template.htmlContent,
          textContent: template.textContent,
        };
        this.bindTemplateFields();
        this.loading = false;
        this.refreshPreview();
      },
      error: (err) => {
        this.toast.error(apiErrorMessage(err, 'Không thể tải mẫu email.'));
        this.loading = false;
      },
    });
  }

  private bindTemplateFields(): void {
    if (!this.template) {
      return;
    }
    this.name = this.template.name;
    this.description = this.template.description;
    this.subject = this.template.subject;
    this.htmlContent = this.template.htmlContent;
    this.textContent = this.template.textContent;
  }

  private toTemplateVariable(variable: EmailTemplateVariableView): EmailTemplateVariable {
    return {
      name: variable.variableName,
      description: variable.description,
      exampleValue: variable.exampleValue,
      required: variable.required,
    };
  }

  private applyLocalPreview(): void {
    this.previewHtml = this.sanitizer.bypassSecurityTrustHtml(
      this.renderTemplate(this.htmlContent),
    );
    this.previewText = this.renderTemplate(this.textContent);
  }

  private renderTemplate(content: string): string {
    return this.variables.reduce((result, variable) => {
      const token = `{{${variable.name}}}`;
      return result.split(token).join(variable.exampleValue);
    }, content);
  }
}
