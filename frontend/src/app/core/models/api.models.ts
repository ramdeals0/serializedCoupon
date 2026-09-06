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
  | 'INVALID_STATUS'
  | 'CHANNEL_NOT_ALLOWED';

export type RedeemChannel = 'POS' | 'ECOMM';

export interface ExternalCouponRequest {
  couponCode: string;
  channel: RedeemChannel;
  locationId?: string | null;
  reference?: string | null;
}

export interface ExternalCouponResponse {
  couponCode: string;
  accepted: boolean;
  markedUsed: boolean;
  validationReason: ValidationReason;
  status: SerializedCouponStatus | null;
  channel: RedeemChannel;
  timesUsed: number;
  usageLimit: number;
  redeemedAt: string | null;
  checkedAt: string;
}

export type CouponSource = 'POS' | 'ECOMM' | 'BOTH';

export type CouponStatus = 'ACTIVE' | 'CANCELLED';

export interface Coupon {
  id: string;
  title: string;
  description: string | null;
  usageLimit: number;
  posCode: string | null;
  atgCode: string | null;
  couponSource: CouponSource;
  rmsCouponId: string;
  rmsCouponCode: string | null;
  rmsCouponName: string;
  couponProgramCode: string;
  startAt: string;
  expiresAt: string;
  status: CouponStatus;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCouponRequest {
  title: string;
  description?: string | null;
  usageLimit: number;
  couponProgramCode: string;
  posCode?: string | null;
  atgCode?: string | null;
  couponSource: CouponSource;
  startAt: string;
  expiresAt: string;
}

export interface CouponBatch {
  id: string;
  couponId: string | null;
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
  couponId: string;
  quantity: number;
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

export type UserRole = 'ADMIN' | 'MANAGER' | 'CUSTOMER_SERVICE';

export type Permission = 'dashboard' | 'batches' | 'create' | 'search' | 'deactivate';

export interface AuthSession {
  token: string;
  tokenType: string;
  expiresAt: string;
  username: string;
  displayName: string;
  role: UserRole;
}
