import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, afterNextRender, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AgriCatalogApiService, EffectivenessLevel, ProductView, PublicProductDetail } from '../../../core/api/agri-catalog-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';

@Component({
  selector: 'app-public-product-detail',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss',
})
export class PublicProductDetailPage {
  private readonly api = inject(AgriCatalogApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private request?: Subscription;
  private productId = '';
  readonly detail = signal<PublicProductDetail | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly detailFields: { key: keyof ProductView; label: string }[] = [
    { key: 'sku', label: 'Mã SKU' }, { key: 'registrationNumber', label: 'Số đăng ký' },
    { key: 'manufacturerName', label: 'Nhà sản xuất' }, { key: 'originCountry', label: 'Xuất xứ' },
    { key: 'form', label: 'Dạng sản phẩm' }, { key: 'unit', label: 'Đơn vị' },
    { key: 'packageSpecification', label: 'Quy cách đóng gói' },
  ];
  readonly descriptionFields: { key: keyof ProductView; label: string }[] = [
    { key: 'description', label: 'Mô tả chi tiết' }, { key: 'ingredients', label: 'Thành phần' },
    { key: 'usageInstruction', label: 'Hướng dẫn sử dụng' }, { key: 'dosageInstruction', label: 'Liều lượng' },
    { key: 'safetyInstruction', label: 'Chỉ dẫn an toàn' }, { key: 'storageInstruction', label: 'Bảo quản' },
    { key: 'warning', label: 'Cảnh báo' },
  ];

  constructor() {
    afterNextRender(() => {
      this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
        this.productId = params.get('productId') ?? '';
        this.load();
      });
    });
  }

  load(): void {
    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.detail.set(null);
    this.request = this.api.getPublicProduct(this.productId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => {
        const detail = unwrapApiResult<PublicProductDetail>(response);
        this.detail.set(detail);
        if (!detail) this.error.set('Không tìm thấy sản phẩm.');
        this.loading.set(false);
      },
      error: error => {
        this.error.set(apiErrorMessage(error, 'Sản phẩm không còn hiển thị hoặc không thể tải.'));
        this.loading.set(false);
      },
    });
  }

  effectivenessLabel(value: EffectivenessLevel | null): string {
    return value ? { LOW: 'Thấp', MEDIUM: 'Trung bình', HIGH: 'Cao', VERY_HIGH: 'Rất cao' }[value] : 'Chưa xác định';
  }

  safePurchaseUrl(value: string | null): string | null {
    if (!value) return null;
    try {
      const url = new URL(value);
      return ['http:', 'https:'].includes(url.protocol) ? url.href : null;
    } catch { return null; }
  }
}
