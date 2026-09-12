const ACCESS_TOKEN_COOKIE = 'access_token';

/**
 * Đọc access_token từ document.cookie (nếu cookie không phải HttpOnly).
 * Dùng cho các service yêu cầu header Authorization: Bearer.
 */
export function readAccessTokenFromCookie(): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const prefix = `${ACCESS_TOKEN_COOKIE}=`;
  const cookies = document.cookie.split(';');

  for (const raw of cookies) {
    const cookie = raw.trim();
    if (cookie.startsWith(prefix)) {
      const value = decodeURIComponent(cookie.slice(prefix.length));
      return value || null;
    }
  }

  return null;
}
