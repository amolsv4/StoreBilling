import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { StoreService } from '../../../core/services/store.service';
import { Store } from '../../../core/models/store.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { StoreFormDialogComponent, StoreFormDialogData } from './store-form-dialog.component';

@Component({
  selector: 'app-store-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './store-list.component.html'
})
export class StoreListComponent {
  private readonly storeService = inject(StoreService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly stores = signal<Store[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Store>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'organizationCode', header: 'Organization' },
    { key: 'businessTypeCode', header: 'Business Type' },
    { key: 'city', header: 'City' },
    { key: 'defaultPriceListId', header: 'Price List', cell: (row) => (row.defaultPriceListId ? '#' + row.defaultPriceListId : 'Not set') }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.storeService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.stores.set(page.content);
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

  openEdit(store: Store): void {
    this.openDialog({ mode: 'edit', store });
  }

  private openDialog(data: StoreFormDialogData): void {
    this.dialog
      .open(StoreFormDialogComponent, { width: '700px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(store: Store): void {
    if (!store.active) {
      this.setActive(store, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate store',
        message: `Deactivate "${store.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(store, false);
      }
    });
  }

  private setActive(store: Store, active: boolean): void {
    const { timezone, addressLine1, addressLine2, city, state, country, postalCode, phone, email, name, defaultPriceListId } = store;
    const request = { timezone, addressLine1, addressLine2, city, state, country, postalCode, phone, email, name, defaultPriceListId, active };
    this.storeService.update(store.id, request).subscribe({
      next: () => {
        this.snackBar.open(`Store ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
