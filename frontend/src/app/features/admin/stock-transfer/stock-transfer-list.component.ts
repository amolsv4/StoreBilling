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
import { StockTransferService } from '../../../core/services/stock-transfer.service';
import { StoreService } from '../../../core/services/store.service';
import { StockTransferResponse, StockTransferStatus } from '../../../core/models/stock-transfer.model';
import { Store } from '../../../core/models/store.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-stock-transfer-list',
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
  templateUrl: './stock-transfer-list.component.html'
})
export class StockTransferListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly stockTransferService = inject(StockTransferService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly transfers = signal<StockTransferResponse[]>([]);
  readonly totalElements = signal(0);
  readonly stores = signal<Store[]>([]);
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.nonNullable.group({
    storeId: [''],
    status: ['']
  });

  readonly columns: DataTableColumn<StockTransferResponse>[] = [
    { key: 'transferNumber', header: 'Transfer #' },
    { key: 'fromStoreName', header: 'From' },
    { key: 'toStoreName', header: 'To' },
    { key: 'transferDate', header: 'Date' },
    { key: 'status', header: 'Status' }
  ];

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const { storeId, status } = this.filterForm.getRawValue();
    this.stockTransferService
      .list(
        { storeId: storeId ? Number(storeId) : undefined, status: (status || undefined) as StockTransferStatus | undefined },
        this.pageIndex,
        this.pageSize
      )
      .subscribe({
        next: (page) => {
          this.transfers.set(page.content);
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
    this.router.navigate(['/admin/stock-transfer', 'new']);
  }

  openDetail(transfer: StockTransferResponse): void {
    this.router.navigate(['/admin/stock-transfer', transfer.id]);
  }
}
