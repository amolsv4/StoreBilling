import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { PosSessionService } from '../../../core/services/pos-session.service';
import { SaleService } from '../../../core/services/sale.service';
import { SaleResponse } from '../../../core/models/sale.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

/** Reuses `DataTableComponent` with the additive `editIcon="play_circle"`/`editLabel="Resume"`
 *  (plan §5) to list/resume `HELD` sales for the current terminal. */
@Component({
  selector: 'app-held-bills-list',
  standalone: true,
  imports: [CommonModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './held-bills-list.component.html'
})
export class HeldBillsListComponent {
  private readonly saleService = inject(SaleService);
  private readonly posSessionService = inject(PosSessionService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly bills = signal<SaleResponse[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<SaleResponse>[] = [
    { key: 'id', header: 'Sale #' },
    { key: 'saleDate', header: 'Held Since', cell: (row) => new Date(row.saleDate).toLocaleString() },
    { key: 'customerId', header: 'Customer', cell: (row) => (row.customerId ? `Customer #${row.customerId}` : 'Walk-in') },
    { key: 'totalAmount', header: 'Total', cell: (row) => row.totalAmount.toFixed(2) }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    const session = this.posSessionService.session();
    if (!session) {
      return;
    }
    this.loading.set(true);
    this.saleService.list({ status: 'HELD', terminalId: session.terminalId }, this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.bills.set(page.content);
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

  resume(sale: SaleResponse): void {
    this.saleService.resume(sale.id).subscribe({
      next: (resumed) => this.router.navigate(['/pos/sale'], { queryParams: { saleId: resumed.id } }),
      error: () => this.snackBar.open('Could not resume this sale.', 'Dismiss', { duration: 4000 })
    });
  }
}
