export const BRAND_UPLOAD_STATUSES = ['UNDER_REVIEW', 'NEEDS_REVISION'] as const;

export const BRAND_USER_STATUS_LABELS: Record<string, string> = {
  PENDING_APPROVAL: 'Chờ xét duyệt',
  UNDER_REVIEW: 'Đang xét duyệt',
  NEEDS_REVISION: 'Cần bổ sung hồ sơ',
  READY_FOR_FINAL_REVIEW: 'Chờ duyệt cuối',
  ACTIVE: 'Đã kích hoạt',
  REJECTED: 'Bị từ chối',
  LOCKED: 'Đã khóa',
  DISABLED: 'Đã vô hiệu hóa',
  DELETED: 'Đã xóa',
};

export const DOC_REVIEW_STATUS_LABELS: Record<string, string> = {
  PENDING_REVIEW: 'Chờ rà soát',
  APPROVED: 'Đã chấp nhận',
  REJECTED: 'Từ chối',
  NEEDS_REVISION: 'Cần tải lại',
};

export function brandUserStatusLabel(status: string): string {
  return BRAND_USER_STATUS_LABELS[status] ?? status;
}

export function docReviewStatusLabel(status: string): string {
  return DOC_REVIEW_STATUS_LABELS[status] ?? status;
}

export function canUploadBrandDocuments(status: string): boolean {
  return (BRAND_UPLOAD_STATUSES as readonly string[]).includes(status);
}

export function brandUserStatusTone(
  status: string
): 'info' | 'warning' | 'success' | 'danger' | 'neutral' {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'NEEDS_REVISION':
      return 'warning';
    case 'REJECTED':
      return 'danger';
    case 'UNDER_REVIEW':
    case 'PENDING_APPROVAL':
    case 'READY_FOR_FINAL_REVIEW':
      return 'info';
    default:
      return 'neutral';
  }
}
