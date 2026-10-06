import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { PriceListService } from '../../../core/services/price-list.service';
import { PriceList } from '../../../core/models/price-list.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { PriceListFormDialogComponent, PriceListFormDialogData } from './price-list-form-dialog.component';

@Component({
  selector: 'app-price-list-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './price-list-list.component.html'
})
export class PriceListListComponent {
  private readonly priceListService = inject(PriceListService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly priceLists = signal<PriceList[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<PriceList>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'organizationName', header: 'Organization' },
    { key: 'effectiveFrom', header: 'Effective From' },
    { key: 'effectiveTo', header: 'Effective To' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.priceListService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.priceLists.set(page.content);
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
    this.openDialog({ mode: 'create' });
  }

  openEdit(priceList: PriceList): void {
    this.openDialog({ mode: 'edit', priceList });
  }

  private openDialog(data: PriceListFormDialogData): void {
    this.dialog
      .open(PriceListFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(priceList: PriceList): void {
    if (!priceList.active) {
      this.setActive(priceList, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate price list',
        message: `Deactivate "${priceList.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(priceList, false);
      }
    });
  }

  private setActive(priceList: PriceList, active: boolean): void {
    const { id, organizationCode, organizationName, ...rest } = priceList;
    this.priceListService.update(id, { ...rest, active }).subscribe({
      next: () => {
        this.snackBar.open(`Price list ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
