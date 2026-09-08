import { Routes } from '@angular/router';
import { authGuard, guestGuard, permissionGuard } from './core/auth/auth.guards';
import { DashboardPage } from './pages/dashboard/dashboard-page';
import { BatchListPage } from './pages/batches/batch-list-page';
import { CreateBatchPage } from './pages/batches/create-batch-page';
import { BatchDetailPage } from './pages/batches/batch-detail-page';
import { CouponSearchPage } from './pages/coupons/coupon-search-page';
import { CouponDetailPage } from './pages/coupons/coupon-detail-page';
import { LoginPage } from './pages/login/login-page';
import { CreateCouponPage } from './pages/offers/create-coupon-page';
import { CouponOfferDetailPage } from './pages/offers/coupon-detail-page';
import { PosDemoPage } from './pages/pos-demo/pos-demo-page';
import { LandingPage } from './pages/landing/landing-page';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'login', component: LoginPage, canActivate: [guestGuard] },
  { path: 'pos-demo', component: PosDemoPage },
  {
    path: 'dashboard',
    redirectTo: '/coupons',
    pathMatch: 'full',
  },
  {
    path: 'coupons/new',
    component: CreateCouponPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'create' },
  },
  {
    path: 'coupons/:couponId/batches/new',
    component: CreateBatchPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'create' },
  },
  {
    path: 'coupons/:couponId',
    component: CouponOfferDetailPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'create' },
  },
  {
    path: 'coupons',
    component: DashboardPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'dashboard' },
  },
  {
    path: 'coupon-batches',
    component: BatchListPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'batches' },
  },
  {
    path: 'coupon-batches/new',
    redirectTo: '/coupons/new',
    pathMatch: 'full',
  },
  {
    path: 'coupon-batches/:batchId',
    component: BatchDetailPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'batches' },
  },
  {
    path: 'serialized-coupons',
    component: CouponSearchPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'search' },
  },
  {
    path: 'serialized-coupons/:couponCode',
    component: CouponDetailPage,
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'search' },
  },
  { path: '**', redirectTo: '' },
];
