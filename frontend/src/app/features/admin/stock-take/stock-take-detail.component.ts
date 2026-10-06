import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StockTakeService } from '../../../core/services/stock-take.service';
import { StoreService } from '../../../core/services/store.service';
import { StockTakeLine, StockTakeResponse } from '../../../core/models/stock-take.model';
import { Store } from '../../../core/models/store.model';
import { Product } from '../../../core/models/product.model';
import { ProductSearchComponent } from '../../../shared/components/product-search/product-search.component';
import {
  LineItemCellChange,
  LineItemsColumn,
  LineItemsEditorComponent
} from '../../../shared/components/line-items-editor/line-items-editor.component';
import { suggestDocumentNumber } from '../../../core/utils/document-number';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

function today(): string {
  return new Date().toISOString().substring(0, 10);
}

/**
 * Same incremental header-then-lines shape as Purchase Receipts/Stock Transfers, but the server
 * snapshots `systemQuantity` per line at add-time (no quantity is sent by the client when adding a
 * line) and `complete()` re-diffs against the *live* balance rather than the stale draft-time
 * snapshot (plan §3, stock-take completion algorithm) - `variance` here is display-only and stays
 * `null` until the stock take is completed.
 */
@Component({
  selector: 'app-stock-take-detail',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ProductSearchComponent,
    LineItemsEditorComponent,
    PageHeaderComponent
  ],
  templateUrl: './stock-take-detail.component.html',
  styleUrl: './stock-take-detail.component.scss'
})
export class StockTakeDetailComponent {
  private readonly fb = inject(FormBuilder);
  private readonly stockTakeService = inject(StockTakeService);
  private readonly storeService = inject(StoreService);
  protected readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly isCreateMode = signal(true);
  readonly loading = signal(false);
  readonly submitting = signal(false);
  readonly busy = signal(false);
  readonly stores = signal<Store[]>([]);
  readonly stockTake = signal<StockTakeResponse | null>(null);

  readonly organizationId = computed(() => this.stockTake()?.organizationId ?? this.authService.currentUser()?.organizationId ?? null);
  /** Counted quantity applied to the next product added; required by the backend at add time. */
  newLineCountedQuantity: number | null = null;
  readonly isDraft = computed(() => this.stockTake()?.status === 'DRAFT');
  readonly linesDisabled = computed(
    () => this.isCreateMode() || !this.isDraft() || !this.authService.hasPermission('STOCK_TAKE_UPDATE')
  );
  readonly canAddLine = computed(() => this.isDraft() && this.authService.hasPermission('STOCK_TAKE_CREATE'));

  readonly headerForm = this.fb.nonNullable.group({
    storeId: [null as number | null, [Validators.required]],
    stockTakeNumber: [suggestDocumentNumber('STK'), [Validators.required, Validators.maxLength(30)]],
    stockTakeDate: [today(), [Validators.required]]
  });

  readonly lineColumns = computed<LineItemsColumn<StockTakeLine>[]>(() => [
    { key: 'productName', header: 'Product', cell: (row) => `${row.productSku} - ${row.productName}` },
    { key: 'systemQuantity', header: 'System Qty', cell: (row) => row.systemQuantity.toFixed(3) },
    { key: 'countedQuantity', header: 'Counted Qty', editable: true, step: 0.001, min: 0 },
    {
      key: 'variance',
      header: 'Variance',
      cell: (row) => (row.variance == null ? 'pending completion' : row.variance.toFixed(3))
    }
  ]);

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));

    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam || idParam === 'new') {
        this.isCreateMode.set(true);
        this.stockTake.set(null);
        return;
      }
      const id = Number(idParam);
      if (Number.isNaN(id)) {
        return;
      }
      this.isCreateMode.set(false);
      this.loadStockTake(id);
    });
  }

  createHeader(): void {
    if (this.headerForm.invalid || this.submitting()) {
      this.headerForm.markAllAsTouched();
      return;
    }
    const raw = this.headerForm.getRawValue();
    // The picked store's organization, not the user's: an org-less super admin has none of their own.
    const orgId = this.stores().find((s) => s.id === raw.storeId)?.organizationId;
    if (!orgId) {
      return;
    }
    this.submitting.set(true);
    this.stockTakeService
      .create({ organizationId: orgId, storeId: raw.storeId as number, stockTakeNumber: raw.stockTakeNumber, stockTakeDate: raw.stockTakeDate })
      .subscribe({
        next: (created) => {
          this.submitting.set(false);
          this.snackBar.open('Stock take created. Add lines below.', 'Dismiss', { duration: 3000 });
          this.router.navigate(['/admin/stock-take', created.id], { replaceUrl: true });
        },
        error: () => this.submitting.set(false)
      });
  }

  saveHeader(): void {
    const current = this.stockTake();
    if (!current || current.status !== 'DRAFT' || this.headerForm.invalid || this.submitting()) {
      return;
    }
    const raw = this.headerForm.getRawValue();
    this.submitting.set(true);
    this.stockTakeService
      .updateHeader(current.id, { stockTakeNumber: raw.stockTakeNumber, stockTakeDate: raw.stockTakeDate })
      .subscribe({
        next: (updated) => {
          this.stockTake.set(updated);
          this.submitting.set(false);
          this.snackBar.open('Header saved.', 'Dismiss', { duration: 3000 });
        },
        error: () => this.submitting.set(false)
      });
  }

  addLine(product: Product): void {
    const current = this.stockTake();
    if (!current) {
      return;
    }
    const counted = this.newLineCountedQuantity;
    if (counted == null || counted < 0) {
      this.snackBar.open('Enter the counted quantity (0 or more) before adding a product.', 'Dismiss', { duration: 4000 });
      return;
    }
    this.busy.set(true);
    this.stockTakeService.addLine(current.id, { productId: product.id, countedQuantity: counted }).subscribe({
      next: (updated) => {
        this.stockTake.set(updated);
        this.newLineCountedQuantity = null;
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  onCellChange(change: LineItemCellChange<StockTakeLine>): void {
    const current = this.stockTake();
    if (!current || change.key !== 'countedQuantity') {
      return;
    }
    this.busy.set(true);
    this.stockTakeService.updateLine(current.id, change.row.id, { countedQuantity: change.value }).subscribe({
      next: (updated) => {
        this.stockTake.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  removeLine(line: StockTakeLine): void {
    const current = this.stockTake();
    if (!current) {
      return;
    }
    this.busy.set(true);
    this.stockTakeService.removeLine(current.id, line.id).subscribe({
      next: (updated) => {
        this.stockTake.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  complete(): void {
    const current = this.stockTake();
    if (!current || current.status !== 'DRAFT' || current.lines.length === 0) {
      return;
    }
    this.busy.set(true);
    this.stockTakeService.complete(current.id).subscribe({
      next: (updated) => {
        this.stockTake.set(updated);
        this.busy.set(false);
        this.snackBar.open('Stock take completed; variances posted to the ledger.', 'Dismiss', { duration: 4000 });
      },
      error: () => this.busy.set(false)
    });
  }

  private loadStockTake(id: number): void {
    this.loading.set(true);
    this.stockTakeService.getById(id).subscribe({
      next: (loaded) => {
        this.stockTake.set(loaded);
        this.headerForm.patchValue({ storeId: loaded.storeId, stockTakeNumber: loaded.stockTakeNumber, stockTakeDate: loaded.stockTakeDate });
        this.headerForm.controls.storeId.disable();
        if (loaded.status !== 'DRAFT') {
          this.headerForm.disable();
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
