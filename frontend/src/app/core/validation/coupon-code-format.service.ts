export class CouponCodeFormatService {
  static readonly PREFIX = 'FF';
  static readonly PROGRAM_CODE_PATTERN = /^\d{4}$/;
  static readonly COUPON_CODE_PATTERN = /^FF[0-9]{4}[A-Z0-9]{8}$/;

  static describe(programCode = '1234'): string {
    return `${this.PREFIX}${programCode} + 8-character suffix = 14 characters`;
  }

  static isValidProgramCode(value: string): boolean {
    return this.PROGRAM_CODE_PATTERN.test(value);
  }

  static isValidCouponCode(value: string): boolean {
    return this.COUPON_CODE_PATTERN.test(value);
  }
}
