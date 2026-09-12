export type EmailPurposeCode =
  | 'REGISTER_OTP'
  | 'RESET_PASSWORD'
  | 'EMAIL_VERIFICATION'
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
    code: 'REGISTER_OTP',
    title: 'Gửi Email xác thực đăng ký',
    description:
      'Sử dụng để gửi mã OTP hoặc link xác thực khi người dùng đăng ký tài khoản mới.',
    configTitle: 'Cấu hình: Gửi Email xác thực đăng ký',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email xác thực tài khoản.',
  },
  {
    code: 'RESET_PASSWORD',
    title: 'Gửi Email khôi phục mật khẩu',
    description: 'Gửi link hoặc mã để người dùng đặt lại mật khẩu khi quên.',
    configTitle: 'Cấu hình: Gửi Email khôi phục mật khẩu',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email khôi phục mật khẩu.',
  },
  {
    code: 'EMAIL_VERIFICATION',
    title: 'Gửi Email xác thực Email',
    description: 'Yêu cầu xác minh địa chỉ email khi có sự thay đổi từ người dùng.',
    configTitle: 'Cấu hình: Gửi Email xác thực Email',
    configSubtitle: 'Quản lý nội dung và các biến số cho mẫu email xác thực địa chỉ.',
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

const DEFAULT_VARIABLES: EmailTemplateVariable[] = [
  {
    name: 'user_name',
    description: 'Họ và tên của người đăng ký',
    exampleValue: 'Nguyễn Văn A',
    required: true,
  },
  {
    name: 'otp_code',
    description: 'Mã xác thực gồm 6 chữ số',
    exampleValue: '123456',
    required: true,
  },
  {
    name: 'expiry_time',
    description: 'Thời gian hết hạn của mã',
    exampleValue: '10 phút',
    required: false,
  },
];

const DEFAULT_TEMPLATES: EmailTemplateItem[] = [
  {
    id: 'tpl-vi-default',
    name: 'Mẫu mặc định (Tiếng Việt)',
    description: 'Mẫu email chuẩn cho quy trình xác thực đăng ký người dùng mới.',
    createdAt: '20/10/2023',
    isActive: true,
  },
  {
    id: 'tpl-en-fallback',
    name: 'Mẫu dự phòng (Tiếng Anh)',
    description: 'English version for international users or fallback scenarios.',
    createdAt: '22/10/2023',
    isActive: false,
  },
];

export function getPurposeByCode(code: string): EmailPurposeCard | undefined {
  return EMAIL_PURPOSES.find((purpose) => purpose.code === code);
}

export function getVariablesByPurpose(_code: EmailPurposeCode): EmailTemplateVariable[] {
  return DEFAULT_VARIABLES.map((item) => ({ ...item }));
}

export function getTemplatesByPurpose(_code: EmailPurposeCode): EmailTemplateItem[] {
  return DEFAULT_TEMPLATES.map((item) => ({ ...item }));
}

const TEMPLATE_DETAILS: Record<string, EmailTemplateDetail> = {
  'tpl-vi-default': {
    id: 'tpl-vi-default',
    name: 'Mẫu mặc định (Tiếng Việt)',
    description: 'Mẫu email chuẩn cho quy trình xác thực đăng ký người dùng mới.',
    createdAt: '20/10/2023',
    isActive: true,
    subject: 'Nông Thịnh - Mã xác thực đăng ký',
    htmlContent: `<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
  <h2 style="color: #1a5c2e;">Chào {{user_name}},</h2>
  <p>Cảm ơn bạn đã đăng ký tài khoản Nông Thịnh.</p>
  <p>Mã xác thực của bạn là:</p>
  <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px;">{{otp_code}}</p>
  <p style="color: #666; font-size: 13px;">Mã có hiệu lực trong {{expiry_time}}.</p>
  <p>Nếu bạn không thực hiện đăng ký, vui lòng bỏ qua email này.</p>
  <p>Trân trọng,<br>Đội ngũ Nông Thịnh</p>
</div>`,
    textContent: `Chào {{user_name}},

Cảm ơn bạn đã đăng ký tài khoản Nông Thịnh.

Mã xác thực của bạn là: {{otp_code}}
Mã có hiệu lực trong {{expiry_time}}.

Nếu bạn không thực hiện đăng ký, vui lòng bỏ qua email này.

Trân trọng,
Đội ngũ Nông Thịnh`,
  },
  'tpl-en-fallback': {
    id: 'tpl-en-fallback',
    name: 'Mẫu dự phòng (Tiếng Anh)',
    description: 'English version for international users or fallback scenarios.',
    createdAt: '22/10/2023',
    isActive: false,
    subject: 'Nong Thinh - Registration Verification Code',
    htmlContent: `<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
  <h2 style="color: #1a5c2e;">Hello {{user_name}},</h2>
  <p>Thank you for registering with Nong Thinh.</p>
  <p>Your verification code is:</p>
  <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px;">{{otp_code}}</p>
  <p style="color: #666; font-size: 13px;">This code expires in {{expiry_time}}.</p>
  <p>If you did not request this, please ignore this email.</p>
  <p>Best regards,<br>Nong Thinh Team</p>
</div>`,
    textContent: `Hello {{user_name}},

Thank you for registering with Nong Thinh.

Your verification code is: {{otp_code}}
This code expires in {{expiry_time}}.

If you did not request this, please ignore this email.

Best regards,
Nong Thinh Team`,
  },
};

export function getTemplateDetail(templateId: string): EmailTemplateDetail | undefined {
  const detail = TEMPLATE_DETAILS[templateId];
  return detail ? { ...detail } : undefined;
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
  <p>Mã xác thực: <strong>{{otp_code}}</strong></p>
  <p style="color: #666; font-size: 13px;">Hiệu lực: {{expiry_time}}</p>
  <p>Trân trọng,<br>Đội ngũ Nông Thịnh</p>
</div>`,
    textContent: `Chào {{user_name}},

Nhập nội dung email tại đây.

Mã xác thực: {{otp_code}}
Hiệu lực: {{expiry_time}}

Trân trọng,
Đội ngũ Nông Thịnh`,
  };
}
