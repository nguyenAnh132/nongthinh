import { DecimalPipe } from '@angular/common';
import {
  ChangeDetectorRef, Component, DestroyRef, ElementRef, OnDestroy, OnInit,
  afterNextRender, inject, input, output, signal, viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  AgriCatalogApiService, DiseaseRecommendation, DiseaseRecommendationPage,
} from '../../../core/api/agri-catalog-api.service';
import { DiagnosisGroup, DiseaseReference } from '../../../core/api/diagnosis-api.service';
import { BrandProfilePublicResponse, ProfileApiService } from '../../../core/api/profile-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';

@Component({
  selector: 'app-recommended-products',
  standalone: true,
  imports: [DecimalPipe, RouterLink],
  templateUrl: './recommended-products.html',
  styleUrl: './recommended-products.scss',
})
export class RecommendedProducts implements OnInit, OnDestroy {
  private static readonly LOAD_THRESHOLD = 0.7;
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly profileApi = inject(ProfileApiService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly viewport = viewChild<ElementRef<HTMLElement>>('viewport');
  private readonly dialog = viewChild<ElementRef<HTMLElement>>('dialog');
  private request?: Subscription;
  private requestVersion = 0;
  private renderFrame?: number;
  private returnFocus: HTMLElement | null = null;
  private readonly requestedBrandIds = new Set<string>();

  readonly groups = input.required<DiagnosisGroup[]>();
  readonly closed = output<void>();
  diseases: DiseaseReference[] = [];
  selectedDiseaseId = '';
  products: DiseaseRecommendation[] = [];
  nextPage = 0;
  hasNext = true;
  loading = false;
  error = '';
  readonly brandNames = signal<Record<string, string>>({});

  constructor() {
    afterNextRender(() => {
      this.returnFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null;
      this.dialog()?.nativeElement.focus();
      this.loadMore();
    });
  }

  ngOnInit(): void {
    this.diseases = [...new Map(this.groups()
      .filter(group => group.status === 'DISEASED' && group.disease?.id)
      .map(group => [group.disease!.id, group.disease!] as const)).values()];
    this.selectedDiseaseId = this.diseases[0]?.id ?? '';
  }

  ngOnDestroy(): void {
    this.requestVersion += 1;
    this.request?.unsubscribe();
    if (this.renderFrame !== undefined) cancelAnimationFrame(this.renderFrame);
    this.returnFocus?.focus();
  }

  selectDisease(diseaseId: string): void {
    if (diseaseId === this.selectedDiseaseId) return;
    this.requestVersion += 1;
    this.request?.unsubscribe();
    this.selectedDiseaseId = diseaseId;
    this.products = [];
    this.nextPage = 0;
    this.hasNext = true;
    this.loading = false;
    this.error = '';
    const viewport = this.viewport()?.nativeElement;
    if (viewport) viewport.scrollTop = 0;
    this.loadMore();
  }

  loadMore(): void {
    if (!this.selectedDiseaseId || this.loading || !this.hasNext) return;
    this.loading = true;
    this.error = '';
    this.changeDetectorRef.markForCheck();
    const version = ++this.requestVersion;
    this.request = this.catalogApi.listDiseaseRecommendations(this.selectedDiseaseId, this.nextPage)
      .subscribe({
        next: response => {
          if (version !== this.requestVersion) return;
          this.loading = false;
          const page = unwrapApiResult<DiseaseRecommendationPage>(response);
          if (!page || !Array.isArray(page.items) || page.page !== this.nextPage) {
            this.error = 'Không thể đọc danh sách sản phẩm. Vui lòng thử lại.';
          } else {
            const existingIds = new Set(this.products.map(item => item.product.id));
            const newItems = page.items.filter(item => !existingIds.has(item.product.id));
            this.products = [...this.products, ...newItems];
            this.loadBrandNames(newItems);
            this.nextPage = page.page + 1;
            this.hasNext = page.hasNext && page.items.length > 0;
            this.renderFrame = requestAnimationFrame(() => this.onScroll());
          }
          this.changeDetectorRef.markForCheck();
        },
        error: error => {
          if (version !== this.requestVersion) return;
          this.loading = false;
          this.error = apiErrorMessage(error, 'Không thể tải sản phẩm được gợi ý.');
          this.changeDetectorRef.markForCheck();
        },
      });
  }

  onScroll(): void {
    const viewport = this.viewport()?.nativeElement;
    if (!viewport || viewport.clientHeight <= 0 || this.error) return;
    if (viewport.scrollTop + viewport.clientHeight >= viewport.scrollHeight * RecommendedProducts.LOAD_THRESHOLD) {
      this.loadMore();
    }
  }

  brandName(brandId: string): string {
    return this.brandNames()[brandId] ?? 'Đang tải thương hiệu...';
  }

  safeUrl(value: string | null | undefined): string | null {
    if (!value) return null;
    return /^https?:\/\//i.test(value) || /^\/(?!\/)/.test(value) ? value : null;
  }

  hideBrokenImage(event: Event): void {
    (event.target as HTMLImageElement).hidden = true;
  }

  private loadBrandNames(items: DiseaseRecommendation[]): void {
    const brandIds = new Set(items.map(item => item.product.brandId).filter(Boolean));
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

  trapFocus(event: KeyboardEvent): void {
    if (event.key !== 'Tab') return;
    const dialog = this.dialog()?.nativeElement;
    const controls = dialog?.querySelectorAll<HTMLElement>('button:not(:disabled), select, a[href]');
    if (!controls?.length) return;
    const first = controls[0];
    const last = controls[controls.length - 1];
    if (event.shiftKey && (document.activeElement === first || document.activeElement === dialog)) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }
}
