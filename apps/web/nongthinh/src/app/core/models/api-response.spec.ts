import { HttpErrorResponse } from '@angular/common/http';
import { apiErrorCode, apiErrorMessage, apiResponseErrorMessage, loginErrorCode } from './api-response';
import { UserFacingError } from './user-facing-error';

describe('User-facing API errors', () => {
  const fallback = 'Không thể tải danh mục bài viết. Vui lòng thử lại.';
  const generic = 'Đã có lỗi xảy ra. Vui lòng thử lại sau.';
  const url = 'http://localhost:4200/api/v1/posts/post-topics';

  it.each([500, 502, 503])('hides HTTP details and backend payloads for status %s', (status) => {
    const error = new HttpErrorResponse({ status, url, statusText: 'Internal Server Error',
      error: { message: 'java.sql.SQLException: database password=secret', detail: url },
    });
    expect(apiErrorMessage(error, fallback)).toBe(generic);
    expect(error.url).toBe(url);
    expect(error.error.message).toContain('SQLException');
  });

  it.each([
    [0, 'Không thể kết nối đến hệ thống. Vui lòng kiểm tra kết nối mạng và thử lại.'],
    [401, 'Phiên đăng nhập đã hết hạn hoặc bạn chưa đăng nhập. Vui lòng đăng nhập lại.'],
    [403, 'Bạn không có quyền thực hiện thao tác này.'],
    [408, 'Yêu cầu xử lý quá lâu. Vui lòng thử lại sau.'],
    [504, 'Yêu cầu xử lý quá lâu. Vui lòng thử lại sau.'],
    [413, 'Tệp hoặc dữ liệu gửi lên vượt quá dung lượng cho phép.'],
    [429, 'Bạn thao tác quá nhanh. Vui lòng chờ một lúc rồi thử lại.'],
  ])('explains HTTP status %s without technical details', (status, expected) => {
    expect(apiErrorMessage(new HttpErrorResponse({ status: Number(status), url }), fallback)).toBe(expected);
  });

  it.each([400, 404, 409, 422])('uses the page fallback for unmapped client errors %s', (status) => {
    expect(apiErrorMessage(new HttpErrorResponse({ status, error: { code: 'UNKNOWN', message: url } }), fallback)).toBe(fallback);
  });

  it.each([null, undefined, 'Http failure response for ' + url, new Error('UPLOAD_URL_MISSING'),
    new TypeError('Cannot read properties of undefined'), { message: url },
    { error: '<html>500 Internal Server Error</html>' },
    { error: { code: 'constructor', message: url } },
  ])('uses a safe fallback for unrecognized errors (%s)', (error) => {
    expect(apiErrorMessage(error, fallback)).toBe(fallback);
  });

  it('keeps known business messages and error codes for existing flows', () => {
    const error = new HttpErrorResponse({ status: 400,
      error: { code: 'BUS_EMAIL_ALREADY_EXISTS', message: 'Email already exists' },
    });
    expect(apiErrorMessage(error, fallback)).toBe('Email đã được sử dụng.');
    expect(apiErrorCode(error)).toBe('BUS_EMAIL_ALREADY_EXISTS');
    expect(loginErrorCode(error)).toBe('OTHER');
    expect(loginErrorCode({ error: { code: 'AUTH_INVALID_CREDENTIALS' } })).toBe('LOGIN_FAILED');
  });

  it('keeps legacy mapped errors', () => {
    expect(apiErrorMessage({ error: { code: 2001 } }, fallback)).toBe('Email đã được sử dụng.');
  });

  it('does not expose message fields from an unsuccessful response envelope', () => {
    expect(apiResponseErrorMessage({ code: 'UNKNOWN', message: url }, fallback)).toBe(fallback);
    expect(apiResponseErrorMessage({ message: 'Internal Server Error' }, fallback)).toBe(fallback);
    expect(apiResponseErrorMessage({ code: 'BUS_EMAIL_ALREADY_EXISTS', message: url }, fallback)).toBe('Email đã được sử dụng.');
  });

  it('preserves explicit local upload validation messages, but not arbitrary Error messages', () => {
    const message = 'Tệp không được vượt quá 3.91 MB.';
    expect(apiErrorMessage(new UserFacingError(message), fallback)).toBe(message);
    expect(apiErrorMessage(new Error(message), fallback)).toBe(fallback);
  });

  it('provides a generic message when the fallback is empty', () => {
    expect(apiErrorMessage(null, ' ')).toBe(generic);
    expect(apiResponseErrorMessage(null, '')).toBe(generic);
  });
});
