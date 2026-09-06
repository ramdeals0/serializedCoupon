import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const COUPON_PROGRAM_CODE_PATTERN = /^\d{4}$/;
export const COUPON_CODE_PATTERN = /^FF[0-9]{4}[A-Z0-9]{8}$/;

export const couponProgramCodeValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const value = control.value as string | null;
  if (value == null || value === '') {
    return null;
  }
  return COUPON_PROGRAM_CODE_PATTERN.test(value) ? null : { couponProgramCode: true };
};

export const sourceCodesValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const source = control.get('couponSource')?.value as string | null;
  const pos = ((control.get('posCode')?.value as string | null) ?? '').trim();
  const atg = ((control.get('atgCode')?.value as string | null) ?? '').trim();
  if (source === 'POS' && !pos) {
    return { posCodeRequired: true };
  }
  if (source === 'ECOMM' && !atg) {
    return { atgCodeRequired: true };
  }
  if (source === 'BOTH' && (!pos || !atg)) {
    return { sourceCodesRequired: true };
  }
  return null;
};

export const startBeforeExpiresValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const startAt = control.get('startAt')?.value as string | null;
  const expiresAt = control.get('expiresAt')?.value as string | null;
  if (!startAt || !expiresAt) {
    return null;
  }
  return new Date(startAt).getTime() < new Date(expiresAt).getTime()
    ? null
    : { startBeforeExpires: true };
};

export function toIsoUtc(dateTimeLocal: string): string {
  return new Date(dateTimeLocal).toISOString();
}

export function toDateTimeLocalValue(iso: string | Date = new Date()): string {
  const date = typeof iso === 'string' ? new Date(iso) : iso;
  const pad = (n: number) => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
