export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface RmsCouponDefinition {
  id: string | null;
  rmsCouponId: string;
  rmsCouponCode: string | null;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}

export type CouponBatchStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'PARTIALLY_COMPLETED'
  | 'FAILED'
  | 'CANCELLED';

export type SerializedCouponStatus =
  | 'ACTIVE'
  | 'PENDING'
  | 'REDEEMED'
  | 'EXPIRED'
  | 'DEACTIVATED'
  | 'CANCELLED';

export type ValidationReason =
  | 'VALID'
  | 'NOT_FOUND'
  | 'NOT_STARTED'
  | 'EXPIRED'
  | 'REDEEMED'
  | 'DEACTIVATED'
  | 'CANCELLED'
  | 'RMS_COUPON_INACTIVE'
  | 'INVALID_STATUS';

export interface CouponBatch {
  id: string;
  rmsCouponId: string;
  rmsCouponCode: string | null;
  rmsCouponName: string;
  couponProgramCode: string;
  requestedQuantity: number;
  generatedQuantity: number;
  startAt: string;
  expiresAt: string;
  status: CouponBatchStatus;
  createdBy: string | null;
  externalReference: string | null;
  createdAt: string;
  updatedAt: string;
  sampleCouponCodes: string[];
}

export interface SerializedCoupon {
  id: string;
  couponCode: string;
  batchId: string;
  rmsCouponId: string;
  rmsCouponCode: string | null;
  couponProgramCode: string;
  startAt: string;
  expiresAt: string;
  status: SerializedCouponStatus;
  redeemedAt: string | null;
  deactivatedAt: string | null;
  externalReference: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCouponBatchRequest {
  rmsCouponId: string;
  couponProgramCode: string;
  quantity: number;
  startAt: string;
  expiresAt: string;
  externalReference?: string | null;
}

export interface CouponValidation {
  couponCode: string;
  valid: boolean;
  validationReason: ValidationReason;
  status: SerializedCouponStatus | null;
  startAt: string | null;
  expiresAt: string | null;
  rmsCouponId: string | null;
  checkedAt: string;
}

export interface DashboardSummary {
  totalBatches: number;
  totalSerializedCoupons: number;
  activeCoupons: number;
  expiredCoupons: number;
  recentBatches: CouponBatch[];
}

export interface GenerationConfig {
  minBatchSize: number;
  maxBatchSize: number;
  prefix: string;
  programCodeLength: number;
  suffixLength: number;
  totalLength: number;
  couponCodeRegex: string;
  programCodeRegex: string;
}

export interface ProblemDetails {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: { field: string; message: string }[];
}
