import { ChangeDetectorRef, Component, NgZone, afterNextRender, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import {
  AdminBrandProfileDetailView,
  BrandProfileApiService,
} from '../../../core/api/brand-profile-api.service';
import {
  BrandApprovalProcessView,
  BrandTicketApiService,
  TicketView,
} from '../../../core/api/brand-ticket-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  approvalProcessStatusLabel,
  brandDocumentReviewStatusLabel,
  brandLifecycleActionLabel,
  brandStatusColor,
  brandStatusLabel,
  canRejectEarly,
  formatDateTime,
  taskKeyLabel,
  ticketBrandProfileId,
  verificationResultLabel,
} from './brand-status.util';

@Component({
  selector: 'app-brand-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    NzAlertModule,
    NzButtonModule,
    NzCardModule,
    NzInputModule,
    NzRadioModule,
    NzSelectModule,
    NzSpinModule,
    NzTagModule,
    NzDividerModule,
  ],
  templateUrl: './brand-detail.html',
  styleUrl: './brand-detail.scss',
})
export class BrandDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly brandProfileApi = inject(BrandProfileApiService);
  private readonly brandTicketApi = inject(BrandTicketApiService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  brandId = '';
  detail: AdminBrandProfileDetailView | null = null;
  process: BrandApprovalProcessView | null = null;
  ticket: TicketView | null = null;

  loading = true;
  actionLoading = false;

  rejectReason = '';
  phoneCalled = '';
  phoneResult: 'VERIFIED' | 'UNREACHABLE' | 'NEED_MORE_INFO' = 'VERIFIED';
  phoneNote = '';
  documentsOk = true;
  revisionReason = '';
  finalOutcome: 'APPROVED' | 'REJECTED' = 'APPROVED';
  finalRejectReason = '';

  readonly canManageTickets = this.auth.hasPermission('ticket:manage');
  readonly currentUserId = this.auth.currentUser()?.userId ?? null;

  statusLabel = brandStatusLabel;
  statusColor = brandStatusColor;
  formatDate = formatDateTime;
  taskLabel = taskKeyLabel;
  lifecycleActionLabel = brandLifecycleActionLabel;
  documentReviewStatusLabel = brandDocumentReviewStatusLabel;
  verificationLabel = verificationResultLabel;
  processStatusLabel = approvalProcessStatusLabel;

  constructor() {
    afterNextRender(() => {
      this.brandId = this.route.snapshot.paramMap.get('id') ?? '';
      if (!this.brandId) {
        this.toast.error('Không tìm thấy hồ sơ doanh nghiệp.');
        this.syncView(() => {
          this.loading = false;
        });
        return;
      }
      this.loadAll();
    });
  }

  loadAll(): void {
    this.loading = true;

    const detail$ = this.brandProfileApi.getDetail(this.brandId);
    const process$ = this.brandTicketApi
      .getProcess(this.brandId)
      .pipe(catchError(() => of({ result: null })));
    const tickets$ = this.canManageTickets
      ? this.brandTicketApi.listTickets().pipe(catchError(() => of({ result: [] as TicketView[] })))
      : of({ result: [] as TicketView[] });

    forkJoin({ detail: detail$, process: process$, tickets: tickets$ })
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: ({ detail, process, tickets }) => {
          this.detail = unwrapApiResult<AdminBrandProfileDetailView>(detail);
          this.process = unwrapApiResult<BrandApprovalProcessView>(process);
          const allTickets = unwrapApiResult<TicketView[]>(tickets) ?? [];
          this.ticket = allTickets.find((t) => ticketBrandProfileId(t) === this.brandId) ?? null;

          if (this.detail?.profile.phone && !this.phoneCalled) {
            this.phoneCalled =
              this.detail.profile.representativePhone || this.detail.profile.phone || '';
          }

          if (!this.detail) {
            this.toast.error(
              apiResponseErrorMessage(
                typeof detail === 'object' && detail !== null && !Array.isArray(detail)
                  ? (detail as { message?: string })
                  : null,
                'Không thể tải chi tiết hồ sơ.',
              ),
            );
          }
        },
        error: (err) => {
          this.detail = null;
          this.toast.error(apiErrorMessage(err, 'Không thể tải chi tiết hồ sơ.'));
        },
      });
  }

  get profile() {
    return this.detail?.profile ?? null;
  }

  get isAssignee(): boolean {
    return !!this.ticket?.assignee && this.ticket.assignee === this.currentUserId;
  }

  get canEarlyReject(): boolean {
    return !!this.profile && canRejectEarly(this.profile.status);
  }

  claimTicket(): void {
    if (!this.ticket) return;
    this.runAction(this.brandTicketApi.claim(this.ticket.taskId), 'Đã nhận công việc để xử lý.');
  }

  unclaimTicket(): void {
    if (!this.ticket) return;
    this.runAction(
      this.brandTicketApi.unclaim(this.ticket.taskId),
      'Đã trả công việc về danh sách chờ.',
    );
  }

  submitEarlyReject(): void {
    const reason = this.rejectReason.trim();
    if (!reason) {
      this.toast.error('Vui lòng nhập lý do từ chối.');
      return;
    }
    this.runAction(
      this.brandProfileApi.rejectEarly(this.brandId, reason),
      'Đã từ chối sớm hồ sơ doanh nghiệp.',
    );
  }

  submitPhoneVerification(): void {
    if (!this.ticket) return;
    if (!/^\d{10}$/.test(this.phoneCalled.trim())) {
      this.toast.error('Số điện thoại phải gồm đúng 10 chữ số.');
      return;
    }
    this.runAction(
      this.brandTicketApi.completePhoneVerification(this.ticket.taskId, {
        phoneCalled: this.phoneCalled.trim(),
        result: this.phoneResult,
        note: this.phoneNote.trim() || null,
      }),
      'Đã hoàn thành xác minh điện thoại.',
    );
  }

  submitDocumentsReview(): void {
    if (!this.ticket) return;
    if (!this.documentsOk && !this.revisionReason.trim()) {
      this.toast.error('Vui lòng nhập lý do yêu cầu bổ sung hồ sơ.');
      return;
    }
    this.runAction(
      this.brandTicketApi.completeDocumentsReview(this.ticket.taskId, {
        documentsOk: this.documentsOk,
        revisionReason: this.documentsOk ? null : this.revisionReason.trim(),
      }),
      this.documentsOk
        ? 'Đã chấp nhận giấy tờ, chuyển sang quyết định cuối.'
        : 'Đã yêu cầu doanh nghiệp bổ sung hồ sơ.',
    );
  }

  submitFinalDecision(): void {
    if (!this.ticket) return;
    if (this.finalOutcome === 'REJECTED' && !this.finalRejectReason.trim()) {
      this.toast.error('Vui lòng nhập lý do từ chối.');
      return;
    }
    this.runAction(
      this.brandTicketApi.completeFinalDecision(this.ticket.taskId, {
        outcome: this.finalOutcome,
        rejectionReason: this.finalOutcome === 'REJECTED' ? this.finalRejectReason.trim() : null,
      }),
      this.finalOutcome === 'APPROVED' ? 'Đã duyệt doanh nghiệp.' : 'Đã từ chối doanh nghiệp.',
    );
  }

  private runAction(request: Observable<unknown>, successMessage: string): void {
    this.actionLoading = true;
    request
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.actionLoading = false;
          });
        }),
      )
      .subscribe({
        next: () => {
          this.toast.success(successMessage);
          this.loadAll();
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Thao tác thất bại.'));
        },
      });
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }
}
