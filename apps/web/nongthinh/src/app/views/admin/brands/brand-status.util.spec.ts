import {
  approvalProcessStatusLabel,
  brandDocumentReviewStatusLabel,
  brandLifecycleActionLabel,
  brandStatusLabel,
  verificationResultLabel,
} from './brand-status.util';

describe('brand status labels', () => {
  it('translates the profile timeline into Vietnamese', () => {
    expect(brandLifecycleActionLabel('STATUS_CHANGED')).toBe('Thay đổi trạng thái');
    expect(brandLifecycleActionLabel('DOCUMENTS_SUBMITTED')).toBe('Nộp giấy tờ');
    expect(brandStatusLabel('PENDING_APPROVAL')).toBe('Chờ duyệt');
    expect(brandStatusLabel('READY_FOR_FINAL_REVIEW')).toBe('Chờ duyệt cuối');
  });

  it('translates related review and process values', () => {
    expect(brandDocumentReviewStatusLabel('PENDING_REVIEW')).toBe('Chờ rà soát');
    expect(verificationResultLabel('UNREACHABLE')).toBe('Không liên lạc được');
    expect(approvalProcessStatusLabel('COMPLETED')).toBe('Đã hoàn tất');
  });

  it('keeps unknown API values visible', () => {
    expect(brandLifecycleActionLabel('NEW_ACTION')).toBe('NEW_ACTION');
    expect(brandStatusLabel('NEW_STATUS')).toBe('NEW_STATUS');
  });
});
