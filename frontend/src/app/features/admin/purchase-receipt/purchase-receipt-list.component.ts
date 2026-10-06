import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PurchaseReceiptService } from '../../../core/services/purchase-receipt.service';
import { StoreService } from '../../../core/services/store.service';
import { PurchaseReceiptResponse, PurchaseReceiptStatus } from '../../../core/models/purchase-receipt.model';
import { Store } from '../../../core/models/store.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

/** List screen; "New Receipt" and row "Edit" both navigate to the routed detail page (plan §5) -
 *  a growing multi-line document doesn't fit `MatDialog` well. */
@Component({
  selector: 'app-purchase-receipt-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    DataTableComponent,
    PageHeaderComponent
  ],
  templateUrl: './purchase-receipt-list.component.html'
})
export class PurchaseReceiptListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly purchaseReceiptService = inject(PurchaseReceiptService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly receipts = signal<PurchaseReceiptResponse[]>([]);
  readonly totalElements = signal(0);
  readonly stores = signal<Store[]>([]);
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.nonNullable.group({
    storeId: [''],
    status: ['']
  });

  readonly columns: DataTableColumn<PurchaseReceiptResponse>[] = [
    { key: 'receiptNumber', header: 'Receipt #' },
    { key: 'storeName', header: 'Store' },
    { key: 'supplierName', header: 'Supplier' },
    { key: 'receiptDate', header: 'Date' },
    { key: 'status', header: 'Status' },
    { key: 'totalAmount', header: 'Total', cell: (row) => row.totalAmount.toFixed(2) }
  ];

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const { storeId, status } = this.filterForm.getRawValue();
    this.purchaseReceiptService
      .list(
        { storeId: storeId ? Number(storeId) : undefined, status: (status || undefined) as PurchaseReceiptStatus | undefined },
        this.pageIndex,
        this.pageSize
      )
      .subscribe({
        next: (page) => {
          this.receipts.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.load();
  }

  resetFilters(): void {
    this.filterForm.reset({ storeId: '', status: '' });
    this.pageIndex = 0;
    this.load();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openCreate(): void {
    this.router.navigate(['/admin/purchase-receipt', 'new']);
  }

  openDetail(receipt: PurchaseReceiptResponse): void {
    this.router.navigate(['/admin/purchase-receipt', receipt.id]);
  }
}
