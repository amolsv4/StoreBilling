import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  path: string;
  icon: string;
  /** Permission code required to see this item; omitted for items visible to any authenticated user. */
  permission?: string;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard', icon: 'dashboard' },
  { label: 'Organizations', path: '/admin/organization', icon: 'business', permission: 'ORGANIZATION_VIEW' },
  { label: 'Stores', path: '/admin/store', icon: 'storefront', permission: 'STORE_VIEW' },
  { label: 'Terminals', path: '/admin/terminal', icon: 'point_of_sale', permission: 'TERMINAL_VIEW' },
  { label: 'Users', path: '/admin/user', icon: 'people', permission: 'USER_VIEW' },
  { label: 'Roles', path: '/admin/role', icon: 'admin_panel_settings', permission: 'ROLE_VIEW' },
  { label: 'Configuration', path: '/admin/configuration', icon: 'tune', permission: 'CONFIGURATION_VIEW' },
  { label: 'Categories', path: '/admin/category', icon: 'category', permission: 'CATEGORY_VIEW' },
  { label: 'Tax Components', path: '/admin/tax-component', icon: 'percent', permission: 'TAX_VIEW' },
  { label: 'Tax Groups', path: '/admin/tax-group', icon: 'receipt_long', permission: 'TAX_VIEW' },
  { label: 'Products', path: '/admin/product', icon: 'inventory_2', permission: 'PRODUCT_VIEW' },
  { label: 'Customers', path: '/admin/customer', icon: 'groups', permission: 'CUSTOMER_VIEW' },
  { label: 'Price Lists', path: '/admin/price-list', icon: 'sell', permission: 'PRICING_VIEW' },
  { label: 'Product Prices', path: '/admin/product-price', icon: 'price_check', permission: 'PRICING_VIEW' },
  // Phase 3 inventory admin screens - corrected permission codes per plan reconciliation note 8
  // (INVENTORY_VIEW/STOCK_TAKE_* underscored forms, not the guessed ADJUSTMENT_*/STOCKTAKE_* names).
  { label: 'Purchase Receipts', path: '/admin/purchase-receipt', icon: 'local_shipping', permission: 'PURCHASE_VIEW' },
  { label: 'Stock Transfers', path: '/admin/stock-transfer', icon: 'sync_alt', permission: 'TRANSFER_VIEW' },
  { label: 'Stock Adjustments', path: '/admin/stock-adjustment', icon: 'tune', permission: 'INVENTORY_VIEW' },
  { label: 'Stock Takes', path: '/admin/stock-take', icon: 'fact_check', permission: 'STOCK_TAKE_VIEW' },
  { label: 'Inventory', path: '/admin/inventory', icon: 'inventory', permission: 'INVENTORY_VIEW' },
  { label: 'Inventory Transactions', path: '/admin/inventory-transaction', icon: 'receipt_long', permission: 'INVENTORY_VIEW' }
];

/** Toolbar + sidenav shell for all authenticated routes; nav items are filtered by permission. */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  private readonly authService = inject(AuthService);

  readonly currentUser = this.authService.currentUser;

  readonly visibleNavItems = computed(() =>
    NAV_ITEMS.filter((item) => !item.permission || this.authService.hasPermission(item.permission))
  );

  /** "Go to POS" is a toolbar button, not a `NAV_ITEMS` sidenav entry - navigating to `/pos`
   *  unmounts this Shell entirely, so a sidenav "active" indicator would be meaningless there
   *  (plan §5). Gated on the same permission that guards the `/pos/sale` route. */
  readonly showPosButton = computed(() => this.authService.hasPermission('SALES_CREATE'));

  readonly displayName = computed(() => {
    const user = this.currentUser();
    if (!user) {
      return '';
    }
    const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ');
    return fullName || user.username;
  });

  logout(): void {
    this.authService.logout();
  }
}
