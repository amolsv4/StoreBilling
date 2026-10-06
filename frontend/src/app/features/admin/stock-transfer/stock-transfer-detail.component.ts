import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StockTransferService } from '../../../core/services/stock-transfer.service';
import { StoreService } from '../../../core/services/store.service';
import { StockTransferLine, StockTransferLineRequest, StockTransferResponse } from '../../../core/models/stock-transfer.model';
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
 * Same incremental header-then-lines shape as Purchase Receipts; `complete()` replaces `receive()`
 * (plan §3) and atomically decrements the from-store / increments the to-store server-side - no
 * IN_TRANSIT state to model here.
 */
@Component({
  selector: 'app-stock-transfer-detail',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
  templateUrl: './stock-transfer-detail.component.html',
  styleUrl: './stock-transfer-detail.component.scss'
})
export class StockTransferDetailComponent {
  private readonly fb = inject(FormBuilder);
  private readonly stockTransferService = inject(StockTransferService);
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
  readonly transfer = signal<StockTransferResponse | null>(null);

  readonly organizationId = computed(() => this.transfer()?.organizationId ?? this.authService.currentUser()?.organizationId ?? null);
  readonly isDraft = computed(() => this.transfer()?.status === 'DRAFT');
  readonly linesDisabled = computed(
    () => this.isCreateMode() || !this.isDraft() || !this.authService.hasPermission('TRANSFER_UPDATE')
  );
  readonly canAddLine = computed(() => this.isDraft() && this.authService.hasPermission('TRANSFER_CREATE'));

  readonly headerForm = this.fb.nonNullable.group({
    fromStoreId: [null as number | null, [Validators.required]],
    toStoreId: [null as number | null, [Validators.required]],
    transferNumber: [suggestDocumentNumber('TRF'), [Validators.required, Validators.maxLength(30)]],
    transferDate: [today(), [Validators.required]]
  });

  readonly lineColumns = computed<LineItemsColumn<StockTransferLine>[]>(() => [
    { key: 'productName', header: 'Product', cell: (row) => `${row.productSku} - ${row.productName}` },
    { key: 'quantity', header: 'Qty', editable: true, step: 0.001, min: 0.001 }
  ]);

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));

    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam || idParam === 'new') {
        this.isCreateMode.set(true);
        this.transfer.set(null);
        return;
      }
      const id = Number(idParam);
      if (Number.isNaN(id)) {
        return;
      }
      this.isCreateMode.set(false);
      this.loadTransfer(id);
    });
  }

  createHeader(): void {
    if (this.headerForm.invalid || this.submitting()) {
      this.headerForm.markAllAsTouched();
      return;
    }
    const raw = this.headerForm.getRawValue();
    // The from-store's organization, not the user's: an org-less super admin has none of their own.
    const orgId = this.stores().find((s) => s.id === raw.fromStoreId)?.organizationId;
    if (!orgId) {
      return;
    }
    if (raw.fromStoreId === raw.toStoreId) {
      this.snackBar.open('From-store and to-store must be different.', 'Dismiss', { duration: 4000 });
      return;
    }
    this.submitting.set(true);
    this.stockTransferService
      .create({
        organizationId: orgId,
        fromStoreId: raw.fromStoreId as number,
        toStoreId: raw.toStoreId as number,
        transferNumber: raw.transferNumber,
        transferDate: raw.transferDate
      })
      .subscribe({
        next: (created) => {
          this.submitting.set(false);
          this.snackBar.open('Stock transfer created. Add lines below.', 'Dismiss', { duration: 3000 });
          this.router.navigate(['/admin/stock-transfer', created.id], { replaceUrl: true });
        },
        error: () => this.submitting.set(false)
      });
  }

  saveHeader(): void {
    const current = this.transfer();
    if (!current || current.status !== 'DRAFT' || this.headerForm.invalid || this.submitting()) {
      return;
    }
    const raw = this.headerForm.getRawValue();
    this.submitting.set(true);
    this.stockTransferService
      .updateHeader(current.id, {
        transferNumber: raw.transferNumber,
        fromStoreId: current.fromStoreId,
        toStoreId: current.toStoreId,
        transferDate: raw.transferDate
      })
      .subscribe({
        next: (updated) => {
          this.transfer.set(updated);
          this.submitting.set(false);
          this.snackBar.open('Header saved.', 'Dismiss', { duration: 3000 });
        },
        error: () => this.submitting.set(false)
      });
  }

  addLine(product: Product): void {
    const current = this.transfer();
    if (!current) {
      return;
    }
    const payload: StockTransferLineRequest = { productId: product.id, quantity: 1 };
    this.busy.set(true);
    this.stockTransferService.addLine(current.id, payload).subscribe({
      next: (updated) => {
        this.transfer.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  onCellChange(change: LineItemCellChange<StockTransferLine>): void {
    const current = this.transfer();
    if (!current) {
      return;
    }
    const payload: StockTransferLineRequest = { productId: change.row.productId, quantity: change.value };
    this.busy.set(true);
    this.stockTransferService.updateLine(current.id, change.row.id, payload).subscribe({
      next: (updated) => {
        this.transfer.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  removeLine(line: StockTransferLine): void {
    const current = this.transfer();
    if (!current) {
      return;
    }
    this.busy.set(true);
    this.stockTransferService.removeLine(current.id, line.id).subscribe({
      next: (updated) => {
        this.transfer.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  complete(): void {
    const current = this.transfer();
    if (!current || current.status !== 'DRAFT' || current.lines.length === 0) {
      return;
    }
    this.busy.set(true);
    this.stockTransferService.complete(current.id).subscribe({
      next: (updated) => {
        this.transfer.set(updated);
        this.busy.set(false);
        this.snackBar.open('Stock transfer completed; both stores updated.', 'Dismiss', { duration: 4000 });
      },
      error: () => this.busy.set(false)
    });
  }

  private loadTransfer(id: number): void {
    this.loading.set(true);
    this.stockTransferService.getById(id).subscribe({
      next: (loaded) => {
        this.transfer.set(loaded);
        this.headerForm.patchValue({
          fromStoreId: loaded.fromStoreId,
          toStoreId: loaded.toStoreId,
          transferNumber: loaded.transferNumber,
          transferDate: loaded.transferDate
        });
        this.headerForm.controls.fromStoreId.disable();
        this.headerForm.controls.toStoreId.disable();
        if (loaded.status !== 'DRAFT') {
          this.headerForm.disable();
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
