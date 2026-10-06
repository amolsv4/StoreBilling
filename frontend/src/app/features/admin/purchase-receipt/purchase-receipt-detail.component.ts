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
import { PurchaseReceiptService } from '../../../core/services/purchase-receipt.service';
import { StoreService } from '../../../core/services/store.service';
import { PurchaseReceiptLine, PurchaseReceiptLineRequest, PurchaseReceiptResponse } from '../../../core/models/purchase-receipt.model';
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
 * Routed detail page (not a dialog - plan §5). Builds a purchase receipt header, then lines one at
 * a time via the incremental endpoints; everything is editable/removable while DRAFT, immutable
 * once RECEIVED. `/admin/purchase-receipt/new` shows the header-only create form; on success the
 * route is replaced with the real id so the lines editor becomes available.
 */
@Component({
  selector: 'app-purchase-receipt-detail',
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
  templateUrl: './purchase-receipt-detail.component.html',
  styleUrl: './purchase-receipt-detail.component.scss'
})
export class PurchaseReceiptDetailComponent {
  private readonly fb = inject(FormBuilder);
  private readonly purchaseReceiptService = inject(PurchaseReceiptService);
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
  readonly receipt = signal<PurchaseReceiptResponse | null>(null);

  readonly organizationId = computed(
    () => this.receipt()?.organizationId ?? this.authService.currentUser()?.organizationId ?? null
  );
  readonly isDraft = computed(() => this.receipt()?.status === 'DRAFT');
  /** Mirrors Sales' own CREATE-vs-UPDATE split (`POST .../lines` = CREATE, `PUT/DELETE .../lines/{id}`
   *  = UPDATE) since the plan's endpoint table doesn't spell out per-sub-action codes for Purchase. */
  readonly linesDisabled = computed(
    () => this.isCreateMode() || !this.isDraft() || !this.authService.hasPermission('PURCHASE_UPDATE')
  );
  readonly canAddLine = computed(() => this.isDraft() && this.authService.hasPermission('PURCHASE_CREATE'));

  readonly headerForm = this.fb.nonNullable.group({
    storeId: [null as number | null, [Validators.required]],
    supplierName: ['', [Validators.required]],
    receiptNumber: [suggestDocumentNumber('PR'), [Validators.required, Validators.maxLength(30)]],
    receiptDate: [today(), [Validators.required]]
  });

  readonly lineColumns = computed<LineItemsColumn<PurchaseReceiptLine>[]>(() => [
    { key: 'productName', header: 'Product', cell: (row) => `${row.productSku} - ${row.productName}` },
    { key: 'quantity', header: 'Qty', editable: true, step: 0.001, min: 0.001 },
    { key: 'unitCost', header: 'Unit Cost', editable: true, step: 0.01, min: 0 },
    { key: 'lineTotal', header: 'Line Total', cell: (row) => row.lineTotal.toFixed(2) }
  ]);

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));

    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (!idParam || idParam === 'new') {
        this.isCreateMode.set(true);
        this.receipt.set(null);
        return;
      }
      const id = Number(idParam);
      if (Number.isNaN(id)) {
        return;
      }
      this.isCreateMode.set(false);
      this.loadReceipt(id);
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
    this.purchaseReceiptService
      .create({
        organizationId: orgId,
        storeId: raw.storeId as number,
        supplierName: raw.supplierName,
        receiptNumber: raw.receiptNumber,
        receiptDate: raw.receiptDate
      })
      .subscribe({
        next: (created) => {
          this.submitting.set(false);
          this.snackBar.open('Purchase receipt created. Add lines below.', 'Dismiss', { duration: 3000 });
          this.router.navigate(['/admin/purchase-receipt', created.id], { replaceUrl: true });
        },
        error: () => this.submitting.set(false)
      });
  }

  saveHeader(): void {
    const current = this.receipt();
    if (!current || current.status !== 'DRAFT' || this.headerForm.invalid || this.submitting()) {
      return;
    }
    const raw = this.headerForm.getRawValue();
    this.submitting.set(true);
    this.purchaseReceiptService
      .updateHeader(current.id, { supplierName: raw.supplierName, receiptNumber: raw.receiptNumber, receiptDate: raw.receiptDate })
      .subscribe({
        next: (updated) => {
          this.receipt.set(updated);
          this.submitting.set(false);
          this.snackBar.open('Header saved.', 'Dismiss', { duration: 3000 });
        },
        error: () => this.submitting.set(false)
      });
  }

  addLine(product: Product): void {
    const current = this.receipt();
    if (!current) {
      return;
    }
    const payload: PurchaseReceiptLineRequest = { productId: product.id, quantity: 1, unitCost: 0 };
    this.busy.set(true);
    this.purchaseReceiptService.addLine(current.id, payload).subscribe({
      next: (updated) => {
        this.receipt.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  onCellChange(change: LineItemCellChange<PurchaseReceiptLine>): void {
    const current = this.receipt();
    if (!current) {
      return;
    }
    const payload: PurchaseReceiptLineRequest = {
      productId: change.row.productId,
      quantity: change.key === 'quantity' ? change.value : change.row.quantity,
      unitCost: change.key === 'unitCost' ? change.value : change.row.unitCost
    };
    this.busy.set(true);
    this.purchaseReceiptService.updateLine(current.id, change.row.id, payload).subscribe({
      next: (updated) => {
        this.receipt.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  removeLine(line: PurchaseReceiptLine): void {
    const current = this.receipt();
    if (!current) {
      return;
    }
    this.busy.set(true);
    this.purchaseReceiptService.removeLine(current.id, line.id).subscribe({
      next: (updated) => {
        this.receipt.set(updated);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  receive(): void {
    const current = this.receipt();
    if (!current || current.status !== 'DRAFT' || current.lines.length === 0) {
      return;
    }
    this.busy.set(true);
    this.purchaseReceiptService.receive(current.id).subscribe({
      next: (updated) => {
        this.receipt.set(updated);
        this.busy.set(false);
        this.snackBar.open('Purchase receipt received; stock updated.', 'Dismiss', { duration: 4000 });
      },
      error: () => this.busy.set(false)
    });
  }

  private loadReceipt(id: number): void {
    this.loading.set(true);
    this.purchaseReceiptService.getById(id).subscribe({
      next: (loaded) => {
        this.receipt.set(loaded);
        this.headerForm.patchValue({
          storeId: loaded.storeId,
          supplierName: loaded.supplierName,
          receiptNumber: loaded.receiptNumber,
          receiptDate: loaded.receiptDate
        });
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
