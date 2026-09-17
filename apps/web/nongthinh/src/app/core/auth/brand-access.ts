export function isBrandAccount(user: { role?: string } | null | undefined): boolean {
  const role = user?.role?.replace(/^ROLE_/, '');
  return role === 'BRAND' || role === 'BRAND_PENDING';
}

export function canUseAppFeatures(
  user: { role?: string; profile?: { status?: string } | null } | null | undefined,
): boolean {
  const role = user?.role?.replace(/^ROLE_/, '');
  return role === 'FARMER' || (role === 'BRAND' && user?.profile?.status === 'ACTIVE');
}
