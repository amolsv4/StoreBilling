import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { TaxComponentService } from '../../../core/services/tax-component.service';
import { TaxComponent } from '../../../core/models/tax-component.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TaxComponentFormDialogComponent, TaxComponentFormDialogData } from './tax-component-form-dialog.component';

@Component({
  selector: 'app-tax-component-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './tax-component-list.component.html'
})
export class TaxComponentListComponent {
  private readonly taxComponentService = inject(TaxComponentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly taxComponents = signal<TaxComponent[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<TaxComponent>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'organizationName', header: 'Organization' },
    { key: 'taxType', header: 'Type' },
    { key: 'percentage', header: 'Percentage (%)' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.taxComponentService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.taxComponents.set(page.content);
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

  openEdit(taxComponent: TaxComponent): void {
    this.openDialog({ mode: 'edit', taxComponent });
  }

  private openDialog(data: TaxComponentFormDialogData): void {
    this.dialog
      .open(TaxComponentFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(taxComponent: TaxComponent): void {
    if (!taxComponent.active) {
      this.setActive(taxComponent, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate tax component',
        message: `Deactivate "${taxComponent.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(taxComponent, false);
      }
    });
  }

  private setActive(taxComponent: TaxComponent, active: boolean): void {
    const { id, organizationCode, organizationName, ...rest } = taxComponent;
    this.taxComponentService.update(id, { ...rest, active }).subscribe({
      next: () => {
        this.snackBar.open(`Tax component ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', {
          duration: 3000
        });
        this.load();
      }
    });
  }
}
