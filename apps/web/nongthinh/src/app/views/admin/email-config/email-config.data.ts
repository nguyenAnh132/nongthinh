export type EmailPurposeCode =
  | 'RESET_PASSWORD'
  | 'BRAND_APPROVED'
  | 'BRAND_REJECTED'
  | 'WELCOME';

export interface EmailPurposeCard {
  code: EmailPurposeCode;
  title: string;
  description: string;
  configTitle: string;
  configSubtitle: string;
}

export interface EmailTemplateVariable {
  name: string;
  description: string;
  exampleValue: string;
  required: boolean;
}

export interface EmailTemplateItem {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  isActive: boolean;
}

export interface EmailTemplateDetail extends EmailTemplateItem {
  subject: string;
  htmlContent: string;
  textContent: string;
}

export const EMAIL_PURPOSES: EmailPurposeCard[] = [
  {
    code: 'RESET_PASSWORD',
    title: 'Gửi Email khôi phục mật khẩu',
    description: 'Gửi link hoặc mã để người dùng đặt lại mật khẩu khi quên.',
    configTitle: 'Cấu hình: Gửi Email khôi phục mật khẩu',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email khôi phục mật khẩu.',
  },
  {
    code: 'BRAND_APPROVED',
    title: 'Gửi Email duyệt thương hiệu đăng ký',
    description:
      'Thông báo cho đối tác khi thương hiệu của họ được hệ thống duyệt thành công.',
    configTitle: 'Cấu hình: Gửi Email duyệt thương hiệu đăng ký',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email thông báo duyệt.',
  },
  {
    code: 'BRAND_REJECTED',
    title: 'Gửi Email từ chối thương hiệu đăng ký',
    description:
      'Thông báo từ chối kèm lý do khi hồ sơ đăng ký thương hiệu không đạt yêu cầu.',
    configTitle: 'Cấu hình: Gửi Email từ chối thương hiệu đăng ký',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email thông báo từ chối.',
  },
  {
    code: 'WELCOME',
    title: 'Gửi Email Chào mừng',
    description: 'Email chào mừng thành viên mới tham gia vào hệ thống Nông Thịnh.',
    configTitle: 'Cấu hình: Gửi Email Chào mừng',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email chào mừng.',
  },
];

export function getPurposeByCode(code: string): EmailPurposeCard | undefined {
  return EMAIL_PURPOSES.find((purpose) => purpose.code === code);
}

export function getVariablesByPurpose(_code: EmailPurposeCode): EmailTemplateVariable[] {
  return [];
}

export function getTemplatesByPurpose(_code: EmailPurposeCode): EmailTemplateItem[] {
  return [];
}

export function createEmptyTemplateDetail(): EmailTemplateDetail {
  return {
    id: 'new',
    name: '',
    description: '',
    createdAt: '',
    isActive: false,
    subject: '',
    htmlContent: `<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
  <h2 style="color: #1a5c2e;">Chào {{user_name}},</h2>
  <p>Nhập nội dung email tại đây.</p>
  <p>Trân trọng,<br>Đội ngũ Nông Thịnh</p>
</div>`,
    textContent: `Chào {{user_name}},

Nhập nội dung email tại đây.

Trân trọng,
Đội ngũ Nông Thịnh`,
  };
}
