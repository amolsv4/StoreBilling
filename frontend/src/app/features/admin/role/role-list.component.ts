import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { RoleService } from '../../../core/services/role.service';
import { Role } from '../../../core/models/role.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { RoleFormDialogComponent, RoleFormDialogData } from './role-form-dialog.component';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatChipsModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './role-list.component.html'
})
export class RoleListComponent {
  private readonly roleService = inject(RoleService);
  private readonly dialog = inject(MatDialog);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly roles = signal<Role[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Role>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'description', header: 'Description' },
    { key: 'permissions', header: 'Permissions', cell: (row) => row.permissions.join(', ') },
    { key: 'systemRole', header: 'System Role', isBoolean: true }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.roleService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.roles.set(page.content);
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

  openEdit(role: Role): void {
    this.openDialog({ mode: 'edit', role });
  }

  private openDialog(data: RoleFormDialogData): void {
    this.dialog
      .open(RoleFormDialogComponent, { width: '560px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
