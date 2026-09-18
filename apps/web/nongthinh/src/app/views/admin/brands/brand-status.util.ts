export const BRAND_PIPELINE_STATUSES = [
  'PENDING_APPROVAL',
  'UNDER_REVIEW',
  'NEEDS_REVISION',
  'READY_FOR_FINAL_REVIEW',
] as const;

export const BRAND_STATUS_LABELS: Record<string, string> = {
  PENDING_APPROVAL: 'Chờ duyệt',
  UNDER_REVIEW: 'Đang xét duyệt',
  NEEDS_REVISION: 'Cần bổ sung',
  READY_FOR_FINAL_REVIEW: 'Chờ duyệt cuối',
  ACTIVE: 'Đã duyệt',
  REJECTED: 'Từ chối',
  DELETED: 'Đã xóa',
};

export const TASK_KEY_LABELS: Record<string, string> = {
  'phone-verification': 'Xác minh điện thoại',
  'documents-review': 'Rà soát giấy tờ',
  'final-decision': 'Quyết định cuối',
};

export const BRAND_LIFECYCLE_ACTION_LABELS: Record<string, string> = {
  STATUS_CHANGED: 'Thay đổi trạng thái',
  DOCUMENT_UPLOADED: 'Tải lên giấy tờ',
  DOCUMENTS_SUBMITTED: 'Nộp giấy tờ',
  DOCUMENTS_REQUESTED: 'Yêu cầu bổ sung giấy tờ',
  DOCUMENT_REVIEWED: 'Rà soát giấy tờ',
  VERIFICATION_RECORDED: 'Ghi nhận kết quả xác minh',
  EARLY_REJECTED: 'Từ chối sớm',
};

export const BRAND_DOCUMENT_REVIEW_STATUS_LABELS: Record<string, string> = {
  PENDING_REVIEW: 'Chờ rà soát',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Đã từ chối',
  NEEDS_REVISION: 'Cần bổ sung',
};

export const VERIFICATION_RESULT_LABELS: Record<string, string> = {
  VERIFIED: 'Đã xác minh',
  UNREACHABLE: 'Không liên lạc được',
  NEED_MORE_INFO: 'Cần thêm thông tin',
};

export const APPROVAL_PROCESS_STATUS_LABELS: Record<string, string> = {
  STARTED: 'Đang xử lý',
  COMPLETED: 'Đã hoàn tất',
  CANCELLED: 'Đã hủy',
};

export function brandStatusLabel(status: string): string {
  return BRAND_STATUS_LABELS[status] ?? status;
}

export function brandStatusColor(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'green';
    case 'PENDING_APPROVAL':
    case 'READY_FOR_FINAL_REVIEW':
      return 'gold';
    case 'UNDER_REVIEW':
      return 'blue';
    case 'NEEDS_REVISION':
      return 'orange';
    case 'REJECTED':
    case 'DELETED':
      return 'red';
    default:
      return 'default';
  }
}

export function taskKeyLabel(key: string): string {
  return TASK_KEY_LABELS[key] ?? key;
}

export function brandLifecycleActionLabel(action: string): string {
  return BRAND_LIFECYCLE_ACTION_LABELS[action] ?? action;
}

export function brandDocumentReviewStatusLabel(status: string): string {
  return BRAND_DOCUMENT_REVIEW_STATUS_LABELS[status] ?? status;
}

export function verificationResultLabel(result: string): string {
  return VERIFICATION_RESULT_LABELS[result] ?? result;
}

export function approvalProcessStatusLabel(status: string): string {
  return APPROVAL_PROCESS_STATUS_LABELS[status] ?? status;
}

export function canRejectEarly(status: string): boolean {
  return !['ACTIVE', 'REJECTED', 'DELETED'].includes(status);
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—';
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? value : d.toLocaleString('vi-VN');
}

export function ticketBrandProfileId(ticket: {
  businessKey: string | null;
  variables: Record<string, unknown> | null;
}): string | null {
  const fromVar = ticket.variables?.['brandProfileId'];
  if (typeof fromVar === 'string' && fromVar) {
    return fromVar;
  }
  if (ticket.businessKey?.startsWith('brand-')) {
    return ticket.businessKey.slice('brand-'.length);
  }
  return ticket.businessKey;
}
