import { CommonModule } from '@angular/common';
import { afterNextRender, ChangeDetectorRef, Component, inject, Input, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  EmailHistoryView,
  EmailTemplatePurposeView,
  EmailTemplateVariableView,
  EmailTemplateView,
  PreviewEmailTemplateView,
  NotificationApiService,
} from '../../../core/api/notification-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  EMAIL_PURPOSES,
  EmailPurposeCard,
  getPurposeByCode,
} from './email-config.data';

type EmailConfigTab = 'config' | 'history';

@Component({
  selector: 'app-email-config',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './email-config.html',
  styleUrl: './email-config.scss',
})
export class EmailConfig {
  @Input() embedded = false;

  private readonly router = inject(Router);
  private readonly notificationApi = inject(NotificationApiService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);
  private readonly toast = inject(ToastService);

  purposes: EmailPurposeCard[] = [];
  activeTab: EmailConfigTab = 'config';

  purposesLoading = true;

  historyLoading = false;
  emailHistory: EmailHistoryView[] = [];

  constructor() {
    afterNextRender(() => {
      this.loadPurposes();
    });
  }

  setTab(tab: EmailConfigTab): void {
    this.activeTab = tab;
    if (tab === 'history' && this.emailHistory.length === 0 && !this.historyLoading) {
      this.loadEmailHistory();
    }
  }

  openPurposeConfig(purpose: EmailPurposeCard): void {
    this.router.navigate(['/admin/email-config', purpose.code]);
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  formatSendAt(iso: string): string {
    return new Date(iso).toLocaleString('vi-VN');
  }

  private loadPurposes(): void {
    this.purposesLoading = true;

    this.notificationApi
      .getEmailTemplatePurposes()
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.purposesLoading = false;
          });
        }),
      )
      .subscribe({
        next: (res) => {
          const data = unwrapApiResult<EmailTemplatePurposeView[]>(res);
          if (data === null) {
            this.purposes = [...EMAIL_PURPOSES];
            this.toast.error(
              apiResponseErrorMessage(
                typeof res === 'object' && res !== null && !Array.isArray(res)
                  ? (res as { message?: string })
                  : null,
                'Không thể tải danh sách mục đích email.',
              ),
            );
            return;
          }

          if (data.length === 0) {
            this.purposes = [...EMAIL_PURPOSES];
            return;
          }

          this.purposes = data.map((purpose) => this.mergePurposeCard(purpose));
        },
        error: (err) => {
          this.purposes = [...EMAIL_PURPOSES];
          this.toast.error(apiErrorMessage(err, 'Không thể tải danh sách mục đích email.'));
        },
      });
  }

  private loadEmailHistory(): void {
    this.historyLoading = true;

    this.notificationApi
      .getAllEmailHistory()
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.historyLoading = false;
          });
        }),
      )
      .subscribe({
        next: (history) => {
          this.emailHistory = Array.isArray(history) ? history : unwrapApiResult<EmailHistoryView[]>(history) ?? [];
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Không thể tải lịch sử gửi mail.'));
        },
      });
  }

  private mergePurposeCard(purpose: EmailTemplatePurposeView): EmailPurposeCard {
    const staticCard = getPurposeByCode(purpose.code);
    return {
      code: purpose.code as EmailPurposeCard['code'],
      title: staticCard?.title ?? purpose.name,
      description: staticCard?.description ?? purpose.description,
      configTitle: staticCard?.configTitle ?? `Cấu hình: ${purpose.name}`,
      configSubtitle: staticCard?.configSubtitle ?? purpose.description,
    };
  }
}
