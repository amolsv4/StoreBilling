/**
 * Suggests an editable document number such as `PR-20261005-143012`. The backend requires the client
 * to supply purchase receipt / stock transfer / stock take numbers (max 30 chars, unique per
 * organization) rather than generating them, so the forms pre-fill this and let the user override it.
 */
export function suggestDocumentNumber(prefix: string): string {
  const now = new Date();
  const two = (n: number): string => String(n).padStart(2, '0');
  const date = `${now.getFullYear()}${two(now.getMonth() + 1)}${two(now.getDate())}`;
  const time = `${two(now.getHours())}${two(now.getMinutes())}${two(now.getSeconds())}`;
  return `${prefix}-${date}-${time}`;
}
