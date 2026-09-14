import { DecimalPipe } from '@angular/common';
import { Component, DestroyRef, afterNextRender, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AgriCatalogApiService, PageView, ProductView } from '../../../core/api/agri-catalog-api.service';
import { BrandProfilePublicResponse, ProfileApiService } from '../../../core/api/profile-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';

@Component({
  selector: 'app-public-products',
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './products.html',
  styleUrl: './products.scss',
})
export class PublicProducts {
  private readonly api = inject(AgriCatalogApiService);
  private readonly profileApi = inject(ProfileApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private request?: Subscription;
  private readonly requestedBrandIds = new Set<string>();
  keyword = '';
  readonly loading = signal(true);
  readonly error = signal('');
  readonly result = signal<PageView<ProductView> | null>(null);
  readonly brandNames = signal<Record<string, string>>({});
  private filters = { keyword: '', page: 0 };

  constructor() {
    afterNextRender(() => {
      this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
        this.keyword = params.get('q') ?? params.get('name') ?? params.get('diseaseName') ?? '';
        const rawPage = Number(params.get('page') ?? 0);
        this.filters = { keyword: this.keyword,
          page: Number.isSafeInteger(rawPage) && rawPage >= 0 ? rawPage : 0 };
        this.load();
      });
    });
  }

  search(): void {
    void this.router.navigate([], { relativeTo: this.route,
      queryParams: { q: this.keyword.trim() || null, name: null, diseaseName: null, page: null } });
  }

  changePage(page: number): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams: { page }, queryParamsHandling: 'merge' });
  }

  load(): void {
    this.request?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.request = this.api.searchPublicProducts(this.filters.keyword, this.filters.page)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: response => {
          const page = unwrapApiResult<PageView<ProductView>>(response);
          this.result.set(page);
          if (!page) {
            this.error.set('Không thể đọc danh sách sản phẩm.');
          } else {
            this.loadBrandNames(page.items);
          }
          this.loading.set(false);
        },
        error: error => {
          this.error.set(apiErrorMessage(error, 'Không thể tải danh sách sản phẩm.'));
          this.loading.set(false);
        },
      });
  }

  hideBrokenImage(event: Event): void {
    (event.target as HTMLImageElement).hidden = true;
  }

  brandName(brandId: string): string {
    return this.brandNames()[brandId] ?? 'Đang tải thương hiệu...';
  }

  private loadBrandNames(products: ProductView[]): void {
    const brandIds = new Set(products.map(product => product.brandId).filter(Boolean));
    for (const brandId of brandIds) {
      if (this.requestedBrandIds.has(brandId)) continue;
      this.requestedBrandIds.add(brandId);
      this.profileApi.getPublicBrandProfileByUserId(brandId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: response => {
            const profile = unwrapApiResult<BrandProfilePublicResponse>(response);
            this.setBrandName(brandId, profile?.brandName?.trim() || 'Thương hiệu chưa cập nhật');
          },
          error: () => this.setBrandName(brandId, 'Thương hiệu chưa cập nhật'),
        });
    }
  }

  private setBrandName(brandId: string, name: string): void {
    this.brandNames.update(current => ({ ...current, [brandId]: name }));
  }
}
