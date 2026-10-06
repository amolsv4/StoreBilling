import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer } from '../../../core/models/customer.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CustomerFormDialogComponent, CustomerFormDialogData } from './customer-form-dialog.component';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './customer-list.component.html'
})
export class CustomerListComponent {
  private readonly customerService = inject(CustomerService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly customers = signal<Customer[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Customer>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'organizationName', header: 'Organization' },
    { key: 'mobile', header: 'Mobile' },
    { key: 'email', header: 'Email' },
    { key: 'creditLimit', header: 'Credit Limit' },
    { key: 'loyaltyPoints', header: 'Loyalty Points' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.customerService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.customers.set(page.content);
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

  openEdit(customer: Customer): void {
    this.openDialog({ mode: 'edit', customer });
  }

  private openDialog(data: CustomerFormDialogData): void {
    this.dialog
      .open(CustomerFormDialogComponent, { width: '700px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(customer: Customer): void {
    if (!customer.active) {
      this.setActive(customer, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate customer',
        message: `Deactivate "${customer.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(customer, false);
      }
    });
  }

  private setActive(customer: Customer, active: boolean): void {
    const { id, organizationId, code, loyaltyPoints, organizationCode, organizationName, ...rest } = customer;
    this.customerService.update(id, { ...rest, active }).subscribe({
      next: () => {
        this.snackBar.open(`Customer ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
