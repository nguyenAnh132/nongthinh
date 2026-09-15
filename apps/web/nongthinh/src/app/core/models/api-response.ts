export interface ApiResponse<T> {
  code?: string | number;
  message?: string;
  result?: T;
}

interface OptionalApiResult<T> {
  present?: boolean;
  value?: T;
}

const SUCCESS_CODES = new Set<string | number>(['1000', 1000]);

export function isApiSuccess(
  response: { code?: string | number | null } | null | undefined,
): boolean {
  if (!response) {
    return false;
  }
  const code = response.code;
  return code == null || SUCCESS_CODES.has(code);
}

/**
 * Giải bọc result từ ApiResponse backend.
 * Hỗ trợ:
 * - ApiResponse { code, message, result }
 * - Optional Java ({ present, value })
 * - Mảng/object trả thẳng (không bọc envelope)
 */
export function unwrapApiResult<T>(response: unknown): T | null {
  if (response == null) {
    return null;
  }

  if (Array.isArray(response)) {
    return response as T;
  }

  if (typeof response !== 'object') {
    return null;
  }

  const envelope = response as { code?: string | number; message?: string; result?: unknown };

  if (!('result' in envelope) && !('code' in envelope)) {
    return response as T;
  }

  if (!isApiSuccess(envelope)) {
    return null;
  }

  const result = envelope.result;
  if (result == null) {
    return null;
  }

  if (typeof result === 'object' && 'present' in result) {
    const wrapped = result as OptionalApiResult<T>;
    return wrapped.present ? (wrapped.value ?? null) : null;
  }

  return result as T;
}

export function apiResponseErrorMessage(
  response: { message?: string } | null | undefined,
  fallback: string,
): string {
  if (response?.message) {
    return response.message;
  }
  return fallback;
}

/** Legacy numeric codes + auth-service string codes (BUS_*, VAL_*, …). */
const ERROR_MESSAGES: Record<string, string> = {
  // Legacy numeric
  '2001': 'Email đã được sử dụng.',
  '2002': 'Email hoặc mật khẩu không đúng.',
  '2003': 'Không tìm thấy người dùng.',
  '2004': 'Bạn không có quyền truy cập.',
  '2005': 'Bạn chưa đăng nhập.',
  '2006': 'Bạn không có quyền thực hiện.',
  '2014': 'Phiên đã hết hạn. Vui lòng đăng nhập lại.',
  '2016': 'Tài khoản đang chờ quản trị viên duyệt.',
  '2017': 'Tài khoản đã bị từ chối. Vui lòng liên hệ quản trị viên.',
  '2018': 'Tài khoản đang bị vô hiệu hoá.',
  '2020': 'Tài khoản đang bị khoá tạm thời do nhập sai mật khẩu nhiều lần.',
  '2021': 'Phiên không hợp lệ. Vui lòng đăng nhập lại.',
  '2034': 'Hệ thống email tạm thời không khả dụng.',

  PRO_PROFILE_STATUS_INVALID: 'Trạng thái hồ sơ không cho phép thao tác này.',
  PRO_BRAND_DOCUMENT_NOT_FOUND: 'Chưa có giấy phép kinh doanh. Vui lòng tải lên trước.',
  PRO_BRAND_DOCUMENT_REVIEW_STATUS_INVALID: 'Giấy phép chưa sẵn sàng để nộp. Vui lòng tải lên lại.',
  PRO_PROFILE_NOT_FOUND: 'Không tìm thấy hồ sơ thương hiệu.',
  FIL_CONTENT_TYPE_NOT_ALLOWED: 'Định dạng file không được hỗ trợ.',
  FIL_FILE_PURPOSE_NOT_ALLOWED: 'Bạn không có quyền tải lên loại file này.',
  FIL_PURPOSE_REQUIRED: 'Thiếu mục đích tải lên file.',
  FIL_PURPOSE_INVALID: 'Mục đích tải lên file không hợp lệ.',
  FIL_FILE_TOO_LARGE: 'File vượt quá dung lượng cho phép.',
  FIL_FILE_EMPTY: 'File tải lên đang trống.',
  FIL_STORAGE_UNAVAILABLE: 'Kho lưu trữ tạm thời không khả dụng.',
  AUTH_BRAND_RESOURCE_ACCESS_DENIED:
    'Bạn không có quyền thao tác trên dữ liệu của thương hiệu khác.',
  BUS_PRODUCT_CATEGORY_SLUG_ALREADY_EXISTS: 'Đường dẫn danh mục đã được sử dụng.',
  BUS_PRODUCT_CATEGORY_PARENT_INVALID: 'Danh mục cha không hợp lệ hoặc tạo thành quan hệ vòng.',
  BUS_PRODUCT_CATEGORY_HAS_CHILDREN: 'Không thể xoá danh mục đang có danh mục con.',
  BUS_PRODUCT_SLUG_ALREADY_EXISTS: 'Đường dẫn sản phẩm đã được sử dụng trong thương hiệu của bạn.',
  BUS_PRODUCT_IMAGE_FILE_ALREADY_USED: 'Ảnh này đã được gắn với một sản phẩm.',
  BUS_PRODUCT_IMAGE_FILE_INVALID: 'Ảnh tải lên không hợp lệ hoặc không còn hoạt động.',
  BUS_POST_TYPE_CODE_ALREADY_EXISTS: 'Mã loại bài viết đã được sử dụng.',
  BUS_POST_TOPIC_SLUG_ALREADY_EXISTS: 'Slug chủ đề đã được sử dụng.',
  BUS_POST_TYPE_IN_USE: 'Không thể xóa loại bài viết đang được bài viết sử dụng.',
  BUS_POST_TOPIC_IN_USE: 'Không thể xóa chủ đề đang được bài viết sử dụng.',
  NOT_FOUND_POST_TYPE_NOT_FOUND: 'Không tìm thấy loại bài viết.',
  NOT_FOUND_POST_TOPIC_NOT_FOUND: 'Không tìm thấy chủ đề.',
  NOT_FOUND_POST_NOT_FOUND: 'Không tìm thấy bài viết.',
  NOT_FOUND_POST_MEDIA_NOT_FOUND: 'Không tìm thấy ảnh của bài viết.',
  NOT_FOUND_POST_CROP_TYPE_NOT_FOUND: 'Loại cây trồng không tồn tại hoặc đã ngừng hoạt động.',
  NOT_FOUND_COMMENT_NOT_FOUND: 'Không tìm thấy bình luận.',
  AUTH_POST_ACCESS_DENIED: 'Bạn không có quyền chỉnh sửa bài viết này.',
  AUTH_COMMENT_ACCESS_DENIED: 'Bạn chỉ có thể sửa hoặc xóa bình luận của mình.',
  BUS_POST_NOT_EDITABLE: 'Trạng thái hiện tại không cho phép chỉnh sửa bài viết.',
  BUS_POST_NOT_PUBLISHABLE: 'Chỉ bài viết nháp mới có thể được đăng.',
  BUS_POST_VISIBILITY_NOT_ALLOWED: 'Chế độ hiển thị này chưa được hỗ trợ.',
  BUS_POST_MEDIA_FILE_INVALID: 'Ảnh bài viết không hợp lệ hoặc không thuộc tài khoản của bạn.',
  BUS_POST_MEDIA_CONFLICT: 'Ảnh hoặc thứ tự ảnh đã được sử dụng trong bài viết.',
  BUS_POST_MEDIA_LIMIT_EXCEEDED: 'Bài viết đã đạt số lượng ảnh tối đa.',
  BUS_POST_REPORT_SELF_NOT_ALLOWED: 'Bạn không thể báo cáo bài viết của chính mình.',
  BUS_POST_REPORT_STATUS_CONFLICT: 'Báo cáo đã được xử lý hoặc không còn ở trạng thái phù hợp.',
  NOT_FOUND_POST_REPORT_NOT_FOUND: 'Không tìm thấy báo cáo bài viết.',
  BUS_COMMENT_NOT_EDITABLE: 'Trạng thái hiện tại không cho phép sửa bình luận.',
  BUS_COMMENT_REPLY_DEPTH_EXCEEDED: 'Bình luận chỉ hỗ trợ một cấp phản hồi.',
  INF_AGRI_CATALOG_SERVICE_UNAVAILABLE: 'Dịch vụ danh mục cây trồng tạm thời không khả dụng.',
  VAL_POST_TYPE_ID_REQUIRED: 'Vui lòng chọn loại bài viết.',
  VAL_POST_CONTENT_REQUIRED: 'Vui lòng nhập nội dung bài viết.',
  VAL_POST_CONTENT_TOO_LONG: 'Nội dung bài viết không được vượt quá 2.000 ký tự.',
  VAL_POST_LOCATION_TOO_LONG: 'Khu vực không được vượt quá 120 ký tự.',
  VAL_POST_CROP_TYPE_IDS_REQUIRED: 'Danh sách cây trồng không hợp lệ.',
  VAL_POST_MEDIA_FILE_ID_REQUIRED: 'Thiếu thông tin ảnh bài viết.',
  VAL_POST_TYPE_CODE_INVALID: 'Mã loại bài phải viết hoa, chỉ gồm chữ, số và dấu gạch dưới.',
  VAL_POST_TOPIC_SLUG_INVALID: 'Slug chủ đề phải viết thường và phân tách bằng dấu gạch ngang.',
  VAL_POST_CATALOG_DISPLAY_ORDER_INVALID: 'Thứ tự hiển thị phải từ 0 trở lên.',
  VAL_COMMENT_CONTENT_REQUIRED: 'Vui lòng nhập nội dung bình luận.',
  VAL_COMMENT_CONTENT_TOO_LONG: 'Bình luận không được vượt quá 2.000 ký tự.',
  VAL_REACTION_TYPE_REQUIRED: 'Vui lòng chọn một cảm xúc hợp lệ.',
  VAL_POST_REPORT_REASON_REQUIRED: 'Vui lòng chọn lý do báo cáo.',
  VAL_POST_REPORT_REASON_DETAIL_INVALID: 'Thông tin bổ sung không được chỉ chứa khoảng trắng.',
  VAL_POST_REPORT_REASON_DETAIL_TOO_LONG: 'Thông tin bổ sung không được vượt quá 10.000 ký tự.',
  VAL_POST_REPORT_RESOLUTION_NOTE_INVALID: 'Ghi chú xử lý không được chỉ chứa khoảng trắng.',
  VAL_POST_REPORT_RESOLUTION_NOTE_TOO_LONG: 'Ghi chú xử lý không được vượt quá 10.000 ký tự.',
  BUS_DISEASE_SLUG_ALREADY_EXISTS: 'Đường dẫn bệnh đã được sử dụng cho loại cây trồng này.',
  BUS_DISEASE_STATUS_TRANSITION_INVALID:
    'Trạng thái hiện tại của bệnh không cho phép thao tác này.',
  BUS_DISEASE_NOT_APPROVED: 'Bệnh phải được quản trị viên duyệt trước khi gắn với sản phẩm.',
  BUS_PRODUCT_DISEASE_TREATMENT_ALREADY_EXISTS: 'Bệnh này đã được khai báo cho sản phẩm.',
  NOT_FOUND_PRODUCT_CATEGORY_NOT_FOUND: 'Không tìm thấy danh mục đã chọn.',
  NOT_FOUND_DISEASE_NOT_FOUND: 'Không tìm thấy thông tin bệnh.',
  INF_FILE_SERVICE_UNAVAILABLE: 'Dịch vụ lưu trữ ảnh tạm thời không khả dụng.',
  VAL_PRODUCT_CATEGORY_NAME_REQUIRED: 'Vui lòng nhập tên danh mục.',
  VAL_PRODUCT_CATEGORY_SLUG_REQUIRED: 'Vui lòng nhập đường dẫn danh mục.',
  VAL_PRODUCT_CATEGORY_DISPLAY_ORDER_INVALID: 'Thứ tự hiển thị phải từ 0 trở lên.',
  VAL_PRODUCT_CATEGORY_ACTIVE_REQUIRED: 'Vui lòng chọn trạng thái hoạt động của danh mục.',
  VAL_PRODUCT_BRAND_ID_REQUIRED: 'Thiếu thông tin thương hiệu.',
  VAL_PRODUCT_CATEGORY_ID_REQUIRED: 'Vui lòng chọn danh mục sản phẩm.',
  VAL_PRODUCT_NAME_REQUIRED: 'Vui lòng nhập tên sản phẩm.',
  VAL_PRODUCT_SLUG_REQUIRED: 'Vui lòng nhập đường dẫn sản phẩm.',
  VAL_DISEASE_NAME_REQUIRED: 'Vui lòng nhập tên bệnh.',
  VAL_DISEASE_SLUG_REQUIRED: 'Vui lòng nhập đường dẫn bệnh.',
  VAL_DISEASE_CROP_TYPE_REQUIRED: 'Vui lòng nhập loại cây trồng.',
  VAL_DISEASE_REJECTION_REASON_REQUIRED: 'Vui lòng nhập lý do từ chối.',
  VAL_PRODUCT_DISEASE_TREATMENT_PRIORITY_INVALID: 'Thứ tự ưu tiên điều trị phải từ 0 trở lên.',

  // auth-service ErrorCode
  BUS_EMAIL_ALREADY_EXISTS: 'Email đã được sử dụng.',
  BUS_EMAIL_INVALID: 'Email không hợp lệ.',
  BUS_EMAIL_FORMAT_INVALID: 'Định dạng email không hợp lệ.',
  BUS_USER_DISABLED: 'Tài khoản đang bị vô hiệu hoá.',
  BUS_USER_LOCKED: 'Tài khoản đang bị khoá tạm thời do nhập sai mật khẩu nhiều lần.',
  NOT_FOUND_USER_NOT_FOUND: 'Không tìm thấy người dùng.',
  AUTH_INVALID_CREDENTIALS: 'Email hoặc mật khẩu không đúng.',
  AUTH_UNAUTHORIZED: 'Bạn chưa đăng nhập.',
  AUTH_UNAUTHENTICATED: 'Bạn chưa đăng nhập hoặc phiên đã hết hạn.',
  AUTH_FORBIDDEN: 'Bạn không có quyền truy cập.',
  UNAUTHENTICATED: 'Bạn chưa đăng nhập hoặc phiên đã hết hạn.',
  INTERNAL_UNAUTHENTICATED: 'Bạn chưa đăng nhập hoặc phiên đã hết hạn.',
  VAL_PHONE_INVALID: 'Số điện thoại không hợp lệ (cần 10 chữ số).',
  VAL_PHONE_REQUIRED: 'Vui lòng nhập số điện thoại.',
  VAL_FIRST_NAME_REQUIRED: 'Vui lòng nhập họ.',
  VAL_LAST_NAME_REQUIRED: 'Vui lòng nhập tên.',
  VAL_LAST_NAME_LENGTH_INVALID: 'Tên phải từ 3 đến 255 ký tự.',
  VAL_FIRST_NAME_LENGTH_INVALID: 'Họ phải từ 1 đến 255 ký tự.',
  VAL_BRAND_NAME_REQUIRED: 'Vui lòng nhập tên thương hiệu.',
  VAL_REPRESENTATIVE_NAME_REQUIRED: 'Vui lòng nhập tên người đại diện.',
  VAL_REPRESENTATIVE_PHONE_REQUIRED: 'Vui lòng nhập SĐT người đại diện.',
  VAL_REPRESENTATIVE_EMAIL_REQUIRED: 'Vui lòng nhập email người đại diện.',
  VAL_REPRESENTATIVE_EMAIL_INVALID: 'Email người đại diện không hợp lệ.',
  SYS_INTERNAL_ERROR: 'Hệ thống đang gặp sự cố. Vui lòng thử lại sau.',
  INF_MODEL_REGISTRY_UNAVAILABLE: 'Dịch vụ danh mục model tạm thời không khả dụng.',
  MODEL_NOT_AVAILABLE_FOR_CROP: 'Chưa có model chẩn đoán đang hoạt động cho loại cây trồng này.',
  SYS_MODEL_NOT_READY: 'Model chưa sẵn sàng hoặc artifact không hợp lệ. Vui lòng thử lại sau.',
  SYS_CATALOG_MAPPING_NOT_READY: 'Model chưa hoàn tất mapping bệnh cho loại cây trồng đã chọn.',
  BUS_AI_MODEL_VERSION_STATE_INVALID:
    'Phiên bản chưa ở đúng bước. Hãy kiểm tra file, hoàn tất mapping rồi cho phép triển khai.',
  BUS_AI_MODEL_VERSION_MAPPING_NOT_READY:
    'Chưa mapping đủ các lớp bệnh cho mọi loại cây thuộc phạm vi model.',
  BUS_AI_MODEL_VERSION_ARTIFACT_INVALID: 'File ONNX không còn hợp lệ hoặc không còn hoạt động.',
  BUS_AI_MODEL_DEPLOYMENT_CROP_COVERAGE_INVALID:
    'Phạm vi triển khai không khớp với phạm vi cây trồng của model.',
  BUS_AI_MODEL_DEPLOYMENT_PRIORITY_CONFLICT:
    'Có thay đổi triển khai đồng thời. Hãy tải lại trạng thái và thử lại.',
  BUS_AI_MODEL_VERSION_HAS_ACTIVE_DEPLOYMENT:
    'Không thể ngừng phiên bản đang phục vụ. Hãy chuyển hoặc tạm dừng deployment trước.',
  SYS_DIAGNOSIS_CAPACITY_EXCEEDED: 'Hàng đợi chẩn đoán đang đầy. Vui lòng thử lại sau ít phút.',
  NOT_FOUND_DIAGNOSIS_HISTORY_NOT_FOUND:
    'Không tìm thấy lịch sử chẩn đoán hoặc bạn không có quyền truy cập.',
  VAL_INVALID_REQUEST_PARAMETER: 'Dữ liệu yêu cầu không hợp lệ.',
};

export type LoginErrorCode =
  | 'USER_STATUS_PENDING'
  | 'USER_STATUS_REJECTED'
  | 'USER_STATUS_DISABLED'
  | 'USER_IS_LOCKED'
  | 'LOGIN_FAILED'
  | 'OTHER';

const LOGIN_CODE_MAP: Record<string, LoginErrorCode> = {
  '2002': 'LOGIN_FAILED',
  '2016': 'USER_STATUS_PENDING',
  '2017': 'USER_STATUS_REJECTED',
  '2018': 'USER_STATUS_DISABLED',
  '2020': 'USER_IS_LOCKED',
  AUTH_INVALID_CREDENTIALS: 'LOGIN_FAILED',
  BUS_USER_DISABLED: 'USER_STATUS_DISABLED',
  BUS_USER_LOCKED: 'USER_IS_LOCKED',
};

interface BackendErrorBody {
  code?: number | string;
  message?: string;
}

interface HttpErrorLike {
  status?: number;
  error?: BackendErrorBody | string;
  message?: string;
}

function extractBackendBody(err: unknown): BackendErrorBody | null {
  const e = err as HttpErrorLike;
  if (!e) return null;
  if (e.error && typeof e.error === 'object') {
    return e.error;
  }
  return null;
}

function normalizeErrorCode(code: number | string | null | undefined): string | null {
  if (code == null) return null;
  return String(code);
}

export function apiErrorCode(err: unknown): string | null {
  const body = extractBackendBody(err);
  return normalizeErrorCode(body?.code ?? null);
}

export function apiErrorMessage(err: unknown, fallback: string): string {
  const body = extractBackendBody(err);
  const code = normalizeErrorCode(body?.code);
  if (code && ERROR_MESSAGES[code]) {
    return ERROR_MESSAGES[code];
  }
  if (body?.message) {
    return body.message;
  }
  const e = err as HttpErrorLike;
  return e?.message ?? fallback;
}

export function loginErrorCode(err: unknown): LoginErrorCode {
  const code = apiErrorCode(err);
  if (code != null && LOGIN_CODE_MAP[code]) {
    return LOGIN_CODE_MAP[code];
  }
  return 'OTHER';
}
