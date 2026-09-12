import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  NgZone,
  afterNextRender,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import gsap from 'gsap';
import {
  BrandProfileApiService,
  BrandProfileView,
} from '../../../core/api/brand-profile-api.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  BRAND_PIPELINE_STATUSES,
  brandStatusColor,
  brandStatusLabel,
  formatDateTime,
} from './brand-status.util';

const PENDING_FILTER = 'pending';

@Component({
  selector: 'app-brands',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    NzTableModule,
    NzTagModule,
    NzButtonModule,
    NzInputModule,
    NzSelectModule,
    NzCardModule,
    NzSpinModule,
  ],
  templateUrl: './brands.html',
  styleUrl: './brands.scss',
})
export class Brands implements AfterViewInit {
  private readonly brandProfileApi = inject(BrandProfileApiService);
  private readonly toast = inject(ToastService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  brands: BrandProfileView[] = [];
  loading = true;

  searchText = '';
  statusFilter = PENDING_FILTER;

  readonly statusOptions = [
    { value: PENDING_FILTER, label: 'Đang xử lý' },
    { value: 'PENDING_APPROVAL', label: 'Chờ duyệt' },
    { value: 'UNDER_REVIEW', label: 'Đang xét duyệt' },
    { value: 'NEEDS_REVISION', label: 'Cần bổ sung' },
    { value: 'READY_FOR_FINAL_REVIEW', label: 'Chờ duyệt cuối' },
    { value: 'ACTIVE', label: 'Đã duyệt' },
    { value: 'REJECTED', label: 'Từ chối' },
    {
      value: [...BRAND_PIPELINE_STATUSES, 'ACTIVE', 'REJECTED', 'DELETED'].join(','),
      label: 'Tất cả',
    },
  ];

  constructor() {
    afterNextRender(() => {
      this.loadBrands();
    });
  }

  loadBrands(): void {
    this.loading = true;

    const request =
      this.statusFilter === PENDING_FILTER
        ? this.brandProfileApi.listPending()
        : this.brandProfileApi.list(this.statusFilter);

    request
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: (res) => {
          const data = unwrapApiResult<BrandProfileView[]>(res);
          if (data === null) {
            this.brands = [];
            this.toast.error(
              apiResponseErrorMessage(
                typeof res === 'object' && res !== null && !Array.isArray(res)
                  ? (res as { message?: string })
                  : null,
                'Không thể tải danh sách doanh nghiệp.',
              ),
            );
            return;
          }

          this.brands = Array.isArray(data) ? data : [];
          setTimeout(() => this.runAnimations(), 50);
        },
        error: (err) => {
          this.brands = [];
          this.toast.error(apiErrorMessage(err, 'Không thể tải danh sách doanh nghiệp.'));
        },
      });
  }

  get filteredBrands(): BrandProfileView[] {
    const q = this.searchText.trim().toLowerCase();
    if (!q) return this.brands;
    return this.brands.filter((b) => {
      const haystack = [
        b.brandName,
        b.taxCode,
        b.representativeName,
        b.representativeEmail,
        b.phone,
        b.id,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(q);
    });
  }

  statusLabel = brandStatusLabel;
  statusColor = brandStatusColor;
  formatDate = formatDateTime;

  ngAfterViewInit(): void {
    if (!this.loading) this.runAnimations();
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  private runAnimations(): void {
    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
    tl.from('.admin-title', { y: -20, autoAlpha: 0, duration: 0.4 })
      .from('.brands-toolbar', { y: 10, autoAlpha: 0, duration: 0.3 }, '-=0.2')
      .from('.ant-table-wrapper', { y: 20, autoAlpha: 0, duration: 0.5 }, '-=0.1');
  }
}
