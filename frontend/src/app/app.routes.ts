import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';
import { posSessionGuard } from './core/guards/pos-session.guard';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent)
  },
  {
    // New top-level area outside `ShellComponent` (plan §5) - no sidenav, cashier-facing.
    path: 'pos',
    loadComponent: () => import('./layout/pos-layout/pos-layout.component').then((m) => m.PosLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'session' },
      {
        path: 'session',
        loadComponent: () =>
          import('./features/pos/session-picker/pos-session-picker.component').then(
            (m) => m.PosSessionPickerComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'SALES_CREATE' }
      },
      {
        path: 'sale',
        loadComponent: () => import('./features/pos/sale/pos-sale.component').then((m) => m.PosSaleComponent),
        canActivate: [permissionGuard, posSessionGuard],
        data: { permission: 'SALES_CREATE' }
      },
      {
        path: 'held-bills',
        loadComponent: () =>
          import('./features/pos/held-bills/held-bills-list.component').then((m) => m.HeldBillsListComponent),
        canActivate: [permissionGuard, posSessionGuard],
        data: { permission: 'SALES_VIEW' }
      }
    ]
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'admin/organization',
        loadComponent: () =>
          import('./features/admin/organization/organization-list.component').then((m) => m.OrganizationListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'ORGANIZATION_VIEW' }
      },
      {
        path: 'admin/store',
        loadComponent: () => import('./features/admin/store/store-list.component').then((m) => m.StoreListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'STORE_VIEW' }
      },
      {
        path: 'admin/terminal',
        loadComponent: () =>
          import('./features/admin/terminal/terminal-list.component').then((m) => m.TerminalListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'TERMINAL_VIEW' }
      },
      {
        path: 'admin/user',
        loadComponent: () => import('./features/admin/user/user-list.component').then((m) => m.UserListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'USER_VIEW' }
      },
      {
        path: 'admin/role',
        loadComponent: () => import('./features/admin/role/role-list.component').then((m) => m.RoleListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'ROLE_VIEW' }
      },
      {
        path: 'admin/configuration',
        loadComponent: () =>
          import('./features/admin/configuration/configuration-page.component').then(
            (m) => m.ConfigurationPageComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'CONFIGURATION_VIEW' }
      },
      {
        path: 'admin/category',
        loadComponent: () =>
          import('./features/admin/category/category-list.component').then((m) => m.CategoryListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'CATEGORY_VIEW' }
      },
      {
        path: 'admin/tax-component',
        loadComponent: () =>
          import('./features/admin/tax-component/tax-component-list.component').then(
            (m) => m.TaxComponentListComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'TAX_VIEW' }
      },
      {
        path: 'admin/tax-group',
        loadComponent: () =>
          import('./features/admin/tax-group/tax-group-list.component').then((m) => m.TaxGroupListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'TAX_VIEW' }
      },
      {
        path: 'admin/product',
        loadComponent: () =>
          import('./features/admin/product/product-list.component').then((m) => m.ProductListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'PRODUCT_VIEW' }
      },
      {
        path: 'admin/customer',
        loadComponent: () =>
          import('./features/admin/customer/customer-list.component').then((m) => m.CustomerListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'CUSTOMER_VIEW' }
      },
      {
        path: 'admin/price-list',
        loadComponent: () =>
          import('./features/admin/price-list/price-list-list.component').then((m) => m.PriceListListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'PRICING_VIEW' }
      },
      {
        path: 'admin/product-price',
        loadComponent: () =>
          import('./features/admin/product-price/product-price-list.component').then(
            (m) => m.ProductPriceListComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'PRICING_VIEW' }
      },
      // Phase 3 inventory admin screens (back-office, inside ShellComponent - plan §5/§6).
      {
        path: 'admin/inventory',
        loadComponent: () =>
          import('./features/admin/inventory/inventory-list.component').then((m) => m.InventoryListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'INVENTORY_VIEW' }
      },
      {
        path: 'admin/inventory-transaction',
        loadComponent: () =>
          import('./features/admin/inventory-transaction/inventory-transaction-list.component').then(
            (m) => m.InventoryTransactionListComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'INVENTORY_VIEW' }
      },
      {
        path: 'admin/stock-adjustment',
        loadComponent: () =>
          import('./features/admin/stock-adjustment/stock-adjustment-list.component').then(
            (m) => m.StockAdjustmentListComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'INVENTORY_VIEW' }
      },
      {
        path: 'admin/purchase-receipt',
        loadComponent: () =>
          import('./features/admin/purchase-receipt/purchase-receipt-list.component').then(
            (m) => m.PurchaseReceiptListComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'PURCHASE_VIEW' }
      },
      {
        // Routed detail page (not a dialog), shared by create ('new') and edit (numeric id).
        path: 'admin/purchase-receipt/:id',
        loadComponent: () =>
          import('./features/admin/purchase-receipt/purchase-receipt-detail.component').then(
            (m) => m.PurchaseReceiptDetailComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'PURCHASE_VIEW' }
      },
      {
        path: 'admin/stock-transfer',
        loadComponent: () =>
          import('./features/admin/stock-transfer/stock-transfer-list.component').then(
            (m) => m.StockTransferListComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'TRANSFER_VIEW' }
      },
      {
        path: 'admin/stock-transfer/:id',
        loadComponent: () =>
          import('./features/admin/stock-transfer/stock-transfer-detail.component').then(
            (m) => m.StockTransferDetailComponent
          ),
        canActivate: [permissionGuard],
        data: { permission: 'TRANSFER_VIEW' }
      },
      {
        path: 'admin/stock-take',
        loadComponent: () =>
          import('./features/admin/stock-take/stock-take-list.component').then((m) => m.StockTakeListComponent),
        canActivate: [permissionGuard],
        data: { permission: 'STOCK_TAKE_VIEW' }
      },
      {
        path: 'admin/stock-take/:id',
        loadComponent: () =>
          import('./features/admin/stock-take/stock-take-detail.component').then((m) => m.StockTakeDetailComponent),
        canActivate: [permissionGuard],
        data: { permission: 'STOCK_TAKE_VIEW' }
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
