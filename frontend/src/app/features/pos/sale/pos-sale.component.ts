import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CustomerService } from '../../../core/services/customer.service';
import { PosSessionService } from '../../../core/services/pos-session.service';
import { SaleService } from '../../../core/services/sale.service';
import { Customer } from '../../../core/models/customer.model';
import { Product } from '../../../core/models/product.model';
import { SaleLine, SaleLineUpdateRequest, SaleResponse } from '../../../core/models/sale.model';
import { CustomerSearchComponent } from '../../../shared/components/customer-search/customer-search.component';
import { ProductSearchComponent } from '../../../shared/components/product-search/product-search.component';
import {
  LineItemCellChange,
  LineItemsColumn,
  LineItemsEditorComponent
} from '../../../shared/components/line-items-editor/line-items-editor.component';
import { PaymentDialogComponent, PaymentDialogData, PaymentDialogResult } from '../payment-dialog/payment-dialog.component';

/**
 * Core POS screen (plan §5). The draft sale is created lazily on the *first* product add - not on
 * screen entry - to avoid littering empty DRAFT sales. The cart always re-renders the server's
 * `SaleResponse` (never recomputes tax/totals locally); the unit-price cell is only editable with
 * `SALES_PRICE_OVERRIDE`. `CurrencyPipe` is bound dynamically to `sale().currency` (reconciliation
 * note 7), not a static environment constant.
 */
@Component({
  selector: 'app-pos-sale',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    ProductSearchComponent,
    CustomerSearchComponent,
    LineItemsEditorComponent
  ],
  templateUrl: './pos-sale.component.html',
  styleUrl: './pos-sale.component.scss'
})
export class PosSaleComponent {
  private readonly authService = inject(AuthService);
  private readonly posSessionService = inject(PosSessionService);
  private readonly saleService = inject(SaleService);
  private readonly customerService = inject(CustomerService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly session = this.posSessionService.session;
  /** The picked store's organization, not the user's - an org-less super admin has none of their own. */
  readonly organizationId = computed(() => this.session()?.organizationId ?? null);
  readonly canOverridePrice = computed(() => this.authService.hasPermission('SALES_PRICE_OVERRIDE'));

  readonly sale = signal<SaleResponse | null>(null);
  readonly selectedCustomer = signal<Customer | null>(null);
  readonly loading = signal(false);
  readonly busy = signal(false);

  billDiscountInput = 0;

  readonly cartColumns = computed<LineItemsColumn<SaleLine>[]>(() => [
    { key: 'productName', header: 'Product', cell: (row) => `${row.sku} - ${row.productName}` },
    { key: 'quantity', header: 'Qty', editable: true, step: 0.001, min: 0.001 },
    { key: 'unitPrice', header: 'Unit Price', editable: this.canOverridePrice(), step: 0.01, min: 0 },
    { key: 'discountAmount', header: 'Discount', editable: true, step: 0.01, min: 0 },
    { key: 'taxPercentage', header: 'Tax %', cell: (row) => row.taxPercentage.toFixed(2) },
    { key: 'taxAmount', header: 'Tax Amt', cell: (row) => row.taxAmount.toFixed(2) },
    { key: 'lineTotal', header: 'Line Total', cell: (row) => row.lineTotal.toFixed(2) }
  ]);

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
      const saleIdParam = params.get('saleId');
      const saleId = saleIdParam ? Number(saleIdParam) : NaN;
      if (!Number.isNaN(saleId)) {
        this.loadExistingSale(saleId);
      }
    });
  }

  addProduct(product: Product): void {
    const currentSale = this.sale();
    if (currentSale) {
      this.busy.set(true);
      this.saleService.addLine(currentSale.id, { productId: product.id, quantity: 1 }).subscribe({
        next: (updated) => {
          this.sale.set(updated);
          this.busy.set(false);
        },
        error: () => this.busy.set(false)
      });
      return;
    }

    const session = this.session();
    const soldByUserId = this.authService.currentUser()?.id;
    if (!session || !soldByUserId) {
      return;
    }

    this.busy.set(true);
    this.saleService
      .create({
        storeId: session.storeId,
        terminalId: session.terminalId,
        customerId: this.selectedCustomer()?.id ?? null
      })
      .pipe(switchMap((created) => this.saleService.addLine(created.id, { productId: product.id, quantity: 1 })))
      .subscribe({
        next: (updated) => {
          this.sale.set(updated);
          this.busy.set(false);
        },
        error: () => this.busy.set(false)
      });
  }

  onCustomerSelected(customer: Customer | null): void {
    this.selectedCustomer.set(customer);
  }

  onCartCellChange(change: LineItemCellChange<SaleLine>): void {
    const currentSale = this.sale();
    if (!currentSale) {
      return;
    }
    const payload: SaleLineUpdateRequest = {
      quantity: change.key === 'quantity' ? change.value : change.row.quantity,
      unitPrice: change.key === 'unitPrice' ? change.value : change.row.unitPrice,
      discountAmount: change.key === 'discountAmount' ? change.value : change.row.discountAmount
    };
    this.busy.set(true);
    this.saleService.updateLine(currentSale.id, change.row.id, payload).subscribe({
      next: (updated) => {
        this.sale.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  onRemoveLine(line: SaleLine): void {
    const currentSale = this.sale();
    if (!currentSale) {
      return;
    }
    this.busy.set(true);
    this.saleService.removeLine(currentSale.id, line.id).subscribe({
      next: (updated) => {
        this.sale.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  applyBillDiscount(): void {
    const currentSale = this.sale();
    if (!currentSale) {
      return;
    }
    this.busy.set(true);
    this.saleService.updateDiscount(currentSale.id, { discountAmount: this.billDiscountInput }).subscribe({
      next: (updated) => {
        this.sale.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  holdSale(): void {
    const currentSale = this.sale();
    if (!currentSale) {
      return;
    }
    this.busy.set(true);
    this.saleService.hold(currentSale.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.snackBar.open('Sale held. Resume it from Held Bills.', 'Dismiss', { duration: 4000 });
        this.resetCart();
        this.router.navigate(['/pos/held-bills']);
      },
      error: () => this.busy.set(false)
    });
  }

  checkout(): void {
    const currentSale = this.sale();
    if (!currentSale || currentSale.lines.length === 0) {
      return;
    }
    const data: PaymentDialogData = { sale: currentSale };
    this.dialog
      .open(PaymentDialogComponent, { width: '560px', disableClose: true, data })
      .afterClosed()
      .subscribe((result: PaymentDialogResult | undefined) => {
        if (result && (result.outcome === 'completed' || result.outcome === 'held')) {
          this.resetCart();
        }
      });
  }

  private resetCart(): void {
    this.sale.set(null);
    this.selectedCustomer.set(null);
    this.billDiscountInput = 0;
  }

  private loadExistingSale(saleId: number): void {
    this.loading.set(true);
    this.saleService.getById(saleId).subscribe({
      next: (loaded) => {
        this.sale.set(loaded);
        this.billDiscountInput = loaded.discountAmount;
        this.loading.set(false);
        if (loaded.customerId) {
          this.customerService.getById(loaded.customerId).subscribe((customer) => this.selectedCustomer.set(customer));
        }
      },
      error: () => this.loading.set(false)
    });
  }
}
