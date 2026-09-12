import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ProductCategoryView,
  ProductPublicationStatus,
  ProductView,
} from '../../../../core/api/agri-catalog-api.service';

export function productStatus(product: ProductView): string {
  const moderationStatus = String(product.moderationStatus ?? 'NORMAL').toUpperCase();
  return moderationStatus === 'LOCKED'
    ? 'LOCKED'
    : String(product.publicationStatus ?? 'DRAFT').toUpperCase();
}

export function productStatusLabel(product: ProductView): string {
  return (
    (
      {
        DRAFT: 'Bản nháp',
        PUBLISHED: 'Đã đăng',
        UNPUBLISHED: 'Đã gỡ đăng',
        LOCKED: 'Bị khóa',
      } as Record<string, string>
    )[productStatus(product)] ?? 'Chưa xác định'
  );
}

@Component({
  selector: 'app-brand-product-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
})
export class BrandProductList {
  @Input() products: ProductView[] = [];
  @Input() categories: ProductCategoryView[] = [];
  @Input() loading = false;
  @Input() error = '';
  @Input() statusUpdatingId: string | null = null;
  @Output() create = new EventEmitter<void>();
  @Output() detail = new EventEmitter<ProductView>();
  @Output() edit = new EventEmitter<ProductView>();
  @Output() history = new EventEmitter<ProductView>();
  @Output() publicationStatusChange = new EventEmitter<{
    product: ProductView;
    status: ProductPublicationStatus;
  }>();
  @Output() retry = new EventEmitter<void>();

  keyword = '';
  category = '';
  status = 'ALL';
  sort = 'newest';
  page = 1;
  readonly pageSize = 10;
  readonly tabs = [
    { value: 'ALL', label: 'Tất cả' },
    { value: 'PUBLISHED', label: 'Đã đăng' },
    { value: 'DRAFT', label: 'Bản nháp' },
    { value: 'UNPUBLISHED', label: 'Đã gỡ đăng' },
    { value: 'LOCKED', label: 'Bị khóa' },
  ];
  readonly statusLabel = productStatusLabel;
  readonly statusKey = productStatus;
  openActionsId: string | null = null;

  @HostListener('document:click')
  closeActions(): void {
    this.openActionsId = null;
  }

  @HostListener('document:keydown.escape')
  closeActionsOnEscape(): void {
    this.closeActions();
  }

  toggleActions(productId: string, event: MouseEvent): void {
    event.stopPropagation();
    this.openActionsId = this.openActionsId === productId ? null : productId;
  }

  chooseAction(action: 'edit' | 'detail' | 'history', product: ProductView): void {
    this.closeActions();
    if (action === 'edit') this.edit.emit(product);
    else if (action === 'detail') this.detail.emit(product);
    else this.history.emit(product);
  }

  publicationActionLabel(product: ProductView): string {
    return productStatus(product) === 'PUBLISHED' ? 'Gỡ đăng' : 'Đăng sản phẩm';
  }

  changePublicationStatus(product: ProductView): void {
    if (product.moderationStatus === 'LOCKED' || this.statusUpdatingId) return;
    this.publicationStatusChange.emit({
      product,
      status: productStatus(product) === 'PUBLISHED' ? 'UNPUBLISHED' : 'PUBLISHED',
    });
  }

  count(status: string): number {
    return this.products.filter((product) => status === 'ALL' || productStatus(product) === status)
      .length;
  }

  get filtered(): ProductView[] {
    const keyword = this.normalize(this.keyword.trim());
    return this.products
      .filter(
        (product) =>
          (this.status === 'ALL' || productStatus(product) === this.status) &&
          (!this.category || product.categoryId === this.category) &&
          (!keyword || this.normalize(`${product.name} ${product.sku ?? ''}`).includes(keyword)),
      )
      .sort((a, b) =>
        this.sort === 'name'
          ? a.name.localeCompare(b.name, 'vi')
          : Date.parse(this.sort === 'updated' ? b.updatedAt : b.createdAt) -
            Date.parse(this.sort === 'updated' ? a.updatedAt : a.createdAt),
      );
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filtered.length / this.pageSize));
  }
  get currentPage(): number {
    return Math.min(this.page, this.totalPages);
  }
  get visible(): ProductView[] {
    return this.filtered.slice(
      (this.currentPage - 1) * this.pageSize,
      this.currentPage * this.pageSize,
    );
  }
  categoryLabel(id: string): string {
    return this.categories.find((item) => item.id === id)?.name ?? 'Danh mục chưa khả dụng';
  }
  resetFilters(): void {
    this.keyword = '';
    this.category = '';
    this.status = 'ALL';
    this.page = 1;
  }
  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/gi, 'd')
      .toLowerCase();
  }
}
