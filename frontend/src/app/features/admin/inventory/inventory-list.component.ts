import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { PageEvent } from '@angular/material/paginator';
import { InventoryService } from '../../../core/services/inventory.service';
import { StoreService } from '../../../core/services/store.service';
import { InventoryBalance } from '../../../core/models/inventory.model';
import { Store } from '../../../core/models/store.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

/** Plain read-only `DataTableComponent` list with a filter bar (plan §5) - current stock balances,
 *  no create/update/delete UI at all. */
@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatInputModule, MatSelectModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './inventory-list.component.html'
})
export class InventoryListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly inventoryService = inject(InventoryService);
  private readonly storeService = inject(StoreService);

  readonly loading = signal(false);
  readonly balances = signal<InventoryBalance[]>([]);
  readonly totalElements = signal(0);
  readonly stores = signal<Store[]>([]);
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.nonNullable.group({
    storeId: [''],
    productId: ['']
  });

  readonly columns: DataTableColumn<InventoryBalance>[] = [
    { key: 'productSku', header: 'SKU' },
    { key: 'productName', header: 'Product' },
    { key: 'storeName', header: 'Store' },
    { key: 'quantityOnHand', header: 'On Hand', cell: (row) => row.quantityOnHand.toFixed(3) },
    { key: 'updatedDate', header: 'Last Updated', cell: (row) => new Date(row.updatedDate).toLocaleString() }
  ];

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const { storeId, productId } = this.filterForm.getRawValue();
    this.inventoryService
      .listBalances(
        {
          storeId: storeId ? Number(storeId) : undefined,
          productId: productId ? Number(productId) : undefined
        },
        this.pageIndex,
        this.pageSize
      )
      .subscribe({
        next: (page) => {
          this.balances.set(page.content);
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
    this.filterForm.reset({ storeId: '', productId: '' });
    this.pageIndex = 0;
    this.load();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }
}
