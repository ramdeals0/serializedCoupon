import { Routes } from '@angular/router';
import { DashboardPage } from './pages/dashboard/dashboard-page';
import { BatchListPage } from './pages/batches/batch-list-page';
import { CreateBatchPage } from './pages/batches/create-batch-page';
import { BatchDetailPage } from './pages/batches/batch-detail-page';
import { CouponSearchPage } from './pages/coupons/coupon-search-page';
import { CouponDetailPage } from './pages/coupons/coupon-detail-page';

export const routes: Routes = [
  { path: '', component: DashboardPage },
  { path: 'coupon-batches', component: BatchListPage },
  { path: 'coupon-batches/new', component: CreateBatchPage },
  { path: 'coupon-batches/:batchId', component: BatchDetailPage },
  { path: 'serialized-coupons', component: CouponSearchPage },
  { path: 'serialized-coupons/:couponCode', component: CouponDetailPage },
  { path: '**', redirectTo: '' },
];
