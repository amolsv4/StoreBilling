import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { TaxGroupService } from '../../../core/services/tax-group.service';
import { TaxGroup } from '../../../core/models/tax-group.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TaxGroupFormDialogComponent, TaxGroupFormDialogData } from './tax-group-form-dialog.component';

@Component({
  selector: 'app-tax-group-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './tax-group-list.component.html'
})
export class TaxGroupListComponent {
  private readonly taxGroupService = inject(TaxGroupService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly taxGroups = signal<TaxGroup[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<TaxGroup>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'organizationName', header: 'Organization' },
    { key: 'totalPercentage', header: 'Total (%)' },
    { key: 'inclusive', header: 'Tax Inclusive', isBoolean: true }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.taxGroupService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.taxGroups.set(page.content);
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

  openEdit(taxGroup: TaxGroup): void {
    this.openDialog({ mode: 'edit', taxGroup });
  }

  private openDialog(data: TaxGroupFormDialogData): void {
    this.dialog
      .open(TaxGroupFormDialogComponent, { width: '700px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(taxGroup: TaxGroup): void {
    if (!taxGroup.active) {
      this.setActive(taxGroup, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate tax group',
        message: `Deactivate "${taxGroup.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(taxGroup, false);
      }
    });
  }

  private setActive(taxGroup: TaxGroup, active: boolean): void {
    const request = {
      name: taxGroup.name,
      description: taxGroup.description,
      componentIds: taxGroup.components.map((c) => c.id),
      active,
      inclusive: taxGroup.inclusive
    };
    this.taxGroupService.update(taxGroup.id, request).subscribe({
      next: () => {
        this.snackBar.open(`Tax group ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
