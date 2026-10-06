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
import { StockTakeService } from '../../../core/services/stock-take.service';
import { StoreService } from '../../../core/services/store.service';
import { StockTakeResponse, StockTakeStatus } from '../../../core/models/stock-take.model';
import { Store } from '../../../core/models/store.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-stock-take-list',
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
  templateUrl: './stock-take-list.component.html'
})
export class StockTakeListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly stockTakeService = inject(StockTakeService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly stockTakes = signal<StockTakeResponse[]>([]);
  readonly totalElements = signal(0);
  readonly stores = signal<Store[]>([]);
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.nonNullable.group({
    storeId: [''],
    status: ['']
  });

  readonly columns: DataTableColumn<StockTakeResponse>[] = [
    { key: 'stockTakeNumber', header: 'Stock Take #' },
    { key: 'storeName', header: 'Store' },
    { key: 'stockTakeDate', header: 'Date' },
    { key: 'status', header: 'Status' }
  ];

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const { storeId, status } = this.filterForm.getRawValue();
    this.stockTakeService
      .list(
        { storeId: storeId ? Number(storeId) : undefined, status: (status || undefined) as StockTakeStatus | undefined },
        this.pageIndex,
        this.pageSize
      )
      .subscribe({
        next: (page) => {
          this.stockTakes.set(page.content);
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
    this.router.navigate(['/admin/stock-take', 'new']);
  }

  openDetail(stockTake: StockTakeResponse): void {
    this.router.navigate(['/admin/stock-take', stockTake.id]);
  }
}
