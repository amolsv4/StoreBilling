import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { Organization } from '../../../core/models/organization.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { OrganizationFormDialogComponent, OrganizationFormDialogData } from './organization-form-dialog.component';

@Component({
  selector: 'app-organization-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './organization-list.component.html'
})
export class OrganizationListComponent {
  private readonly organizationService = inject(OrganizationService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly organizations = signal<Organization[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Organization>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'city', header: 'City' },
    { key: 'country', header: 'Country' },
    { key: 'contactEmail', header: 'Contact Email' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.organizationService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.organizations.set(page.content);
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

  openEdit(organization: Organization): void {
    this.openDialog({ mode: 'edit', organization });
  }

  private openDialog(data: OrganizationFormDialogData): void {
    this.dialog
      .open(OrganizationFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(organization: Organization): void {
    if (!organization.active) {
      this.reactivate(organization);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate organization',
        message: `Deactivate "${organization.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(organization, false);
      }
    });
  }

  private reactivate(organization: Organization): void {
    this.setActive(organization, true);
  }

  private setActive(organization: Organization, active: boolean): void {
    const { name, legalName, taxRegistrationNo, addressLine1, addressLine2, city, state, country, postalCode, contactEmail, contactPhone } = organization;
    const request = { name, legalName, taxRegistrationNo, addressLine1, addressLine2, city, state, country, postalCode, contactEmail, contactPhone, active };
    this.organizationService.update(organization.id, request).subscribe({
      next: () => {
        this.snackBar.open(`Organization ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
