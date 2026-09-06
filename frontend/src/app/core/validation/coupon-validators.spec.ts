import { FormControl, FormGroup } from '@angular/forms';
import {
  couponProgramCodeValidator,
  sourceCodesValidator,
  startBeforeExpiresValidator,
} from './coupon-validators';

describe('coupon validators', () => {
  it('rejects a program code that is not exactly four digits', () => {
    const control = new FormControl('12ab');
    expect(couponProgramCodeValidator(control)).toEqual({ couponProgramCode: true });
  });

  it('accepts a four-digit program code including leading zeros', () => {
    expect(couponProgramCodeValidator(new FormControl('0001'))).toBeNull();
    expect(couponProgramCodeValidator(new FormControl('1234'))).toBeNull();
  });

  it('rejects an expiration that is not after start', () => {
    const group = new FormGroup(
      {
        startAt: new FormControl('2026-12-31T10:00'),
        expiresAt: new FormControl('2026-01-01T10:00'),
      },
      { validators: startBeforeExpiresValidator },
    );
    expect(group.errors).toEqual({ startBeforeExpires: true });
  });

  it('accepts expiration after start', () => {
    const group = new FormGroup(
      {
        startAt: new FormControl('2026-01-01T10:00'),
        expiresAt: new FormControl('2026-12-31T10:00'),
      },
      { validators: startBeforeExpiresValidator },
    );
    expect(group.errors).toBeNull();
  });

  it('requires POS and ATG codes when source is Both', () => {
    const group = new FormGroup(
      {
        couponSource: new FormControl('BOTH'),
        posCode: new FormControl(''),
        atgCode: new FormControl(''),
      },
      { validators: sourceCodesValidator },
    );
    expect(group.errors).toEqual({ sourceCodesRequired: true });
  });
});
