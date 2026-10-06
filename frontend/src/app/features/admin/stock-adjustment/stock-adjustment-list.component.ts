import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { StockAdjustmentService } from '../../../core/services/stock-adjustment.service';
import { StockAdjustment } from '../../../core/models/stock-adjustment.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StockAdjustmentFormDialogComponent } from './stock-adjustment-form-dialog.component';

@Component({
  selector: 'app-stock-adjustment-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './stock-adjustment-list.component.html'
})
export class StockAdjustmentListComponent {
  private readonly stockAdjustmentService = inject(StockAdjustmentService);
  private readonly dialog = inject(MatDialog);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly adjustments = signal<StockAdjustment[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<StockAdjustment>[] = [
    { key: 'productSku', header: 'SKU' },
    { key: 'productName', header: 'Product' },
    { key: 'storeName', header: 'Store' },
    { key: 'adjustmentType', header: 'Type' },
    { key: 'quantityChange', header: 'Qty Change', cell: (row) => row.quantityChange.toFixed(3) },
    { key: 'reason', header: 'Reason' },
    { key: 'createdDate', header: 'Date', cell: (row) => new Date(row.createdDate).toLocaleString() }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.stockAdjustmentService.list(undefined, this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.adjustments.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openCreate(): void {
    this.dialog
      .open(StockAdjustmentFormDialogComponent, { width: '640px', disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
