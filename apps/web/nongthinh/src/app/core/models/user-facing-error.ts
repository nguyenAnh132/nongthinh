/** A message authored by the frontend for users, never copied from a backend response. */
export class UserFacingError extends Error {
  override readonly name = 'UserFacingError';
}
