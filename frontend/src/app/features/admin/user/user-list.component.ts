import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { AppUser } from '../../../core/models/user.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { UserFormDialogComponent, UserFormDialogData } from './user-form-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './user-list.component.html'
})
export class UserListComponent {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly users = signal<AppUser[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<AppUser>[] = [
    { key: 'username', header: 'Username' },
    { key: 'email', header: 'Email' },
    { key: 'firstName', header: 'First Name' },
    { key: 'lastName', header: 'Last Name' },
    { key: 'roleCodes', header: 'Roles', cell: (row) => row.roleCodes.join(', ') },
    { key: 'allStoreAccess', header: 'All Stores', isBoolean: true },
    { key: 'accountLocked', header: 'Locked', isBoolean: true }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.userService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.users.set(page.content);
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

  openEdit(user: AppUser): void {
    this.openDialog({ mode: 'edit', user });
  }

  private openDialog(data: UserFormDialogData): void {
    this.dialog
      .open(UserFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(user: AppUser): void {
    if (!user.active) {
      this.setActive(user, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate user',
        message: `Deactivate "${user.username}"? They will no longer be able to log in.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(user, false);
      }
    });
  }

  private setActive(user: AppUser, active: boolean): void {
    const { firstName, lastName, phone, organizationId, allStoreAccess, accountLocked, storeIds, roleCodes } = user;
    const request = { firstName, lastName, phone: phone ?? '', organizationId, allStoreAccess, accountLocked, storeIds, roleCodes, active };
    this.userService.update(user.id, request).subscribe({
      next: () => {
        this.snackBar.open(`User ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
