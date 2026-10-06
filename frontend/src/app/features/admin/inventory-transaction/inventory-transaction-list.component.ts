import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { PageEvent } from '@angular/material/paginator';
import { InventoryService } from '../../../core/services/inventory.service';
import { StoreService } from '../../../core/services/store.service';
import { INVENTORY_TRANSACTION_TYPES, InventoryTransaction, InventoryTransactionType } from '../../../core/models/inventory.model';
import { Store } from '../../../core/models/store.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

/** Plain read-only append-only ledger view (plan §5) - no create/update/delete UI. */
@Component({
  selector: 'app-inventory-transaction-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatInputModule, MatSelectModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './inventory-transaction-list.component.html'
})
export class InventoryTransactionListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly inventoryService = inject(InventoryService);
  private readonly storeService = inject(StoreService);

  readonly transactionTypes = INVENTORY_TRANSACTION_TYPES;
  readonly loading = signal(false);
  readonly transactions = signal<InventoryTransaction[]>([]);
  readonly totalElements = signal(0);
  readonly stores = signal<Store[]>([]);
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.nonNullable.group({
    storeId: [''],
    productId: [''],
    type: ['']
  });

  readonly columns: DataTableColumn<InventoryTransaction>[] = [
    { key: 'productSku', header: 'SKU' },
    { key: 'productName', header: 'Product' },
    { key: 'storeName', header: 'Store' },
    { key: 'transactionType', header: 'Type' },
    { key: 'quantityChange', header: 'Change', cell: (row) => row.quantityChange.toFixed(3) },
    { key: 'balanceAfter', header: 'Balance After', cell: (row) => row.balanceAfter.toFixed(3) },
    { key: 'referenceType', header: 'Reference', cell: (row) => (row.referenceType ? `${row.referenceType} #${row.referenceId}` : '') },
    { key: 'createdDate', header: 'Date', cell: (row) => new Date(row.createdDate).toLocaleString() }
  ];

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const { storeId, productId, type } = this.filterForm.getRawValue();
    this.inventoryService
      .listTransactions(
        {
          storeId: storeId ? Number(storeId) : undefined,
          productId: productId ? Number(productId) : undefined,
          type: (type || undefined) as InventoryTransactionType | undefined
        },
        this.pageIndex,
        this.pageSize
      )
      .subscribe({
        next: (page) => {
          this.transactions.set(page.content);
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
    this.filterForm.reset({ storeId: '', productId: '', type: '' });
    this.pageIndex = 0;
    this.load();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }
}
