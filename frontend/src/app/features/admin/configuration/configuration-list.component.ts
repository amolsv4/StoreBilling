import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { ConfigService } from '../../../core/services/config.service';
import { ConfigurationItem, SCOPE_TYPES, ScopeType } from '../../../core/models/configuration.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import {
  ConfigurationFormDialogComponent,
  ConfigurationFormDialogData
} from './configuration-form-dialog.component';

@Component({
  selector: 'app-configuration-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    DataTableComponent,
    PageHeaderComponent
  ],
  templateUrl: './configuration-list.component.html'
})
export class ConfigurationListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly configService = inject(ConfigService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly scopeTypes = SCOPE_TYPES;
  readonly loading = signal(false);
  readonly configurations = signal<ConfigurationItem[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly filterForm = this.fb.nonNullable.group({
    key: [''],
    scopeType: [''],
    scopeId: ['']
  });

  readonly columns: DataTableColumn<ConfigurationItem>[] = [
    { key: 'configKey', header: 'Key' },
    { key: 'scopeType', header: 'Scope Type' },
    { key: 'scopeName', header: 'Scope', cell: (row) => row.scopeName ?? (row.scopeType === 'GLOBAL' ? 'GLOBAL' : String(row.scopeId ?? '')) },
    { key: 'dataType', header: 'Data Type' },
    { key: 'configValue', header: 'Value' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const { key, scopeType, scopeId } = this.filterForm.getRawValue();
    this.configService
      .list({
        key: key || undefined,
        scopeType: (scopeType || undefined) as ScopeType | undefined,
        scopeId: scopeId ? Number(scopeId) : undefined,
        page: this.pageIndex,
        size: this.pageSize
      })
      .subscribe({
        next: (page) => {
          this.configurations.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.load();
  }

  resetFilters(): void {
    this.filterForm.reset({ key: '', scopeType: '', scopeId: '' });
    this.pageIndex = 0;
    this.load();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openCreate(): void {
    this.openDialog({ mode: 'create' });
  }

  openEdit(configuration: ConfigurationItem): void {
    this.openDialog({ mode: 'edit', configuration });
  }

  private openDialog(data: ConfigurationFormDialogData): void {
    this.dialog
      .open(ConfigurationFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(configuration: ConfigurationItem): void {
    if (!configuration.active) {
      this.setActive(configuration, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate configuration',
        message: `Deactivate "${configuration.configKey}" at scope ${configuration.scopeType}? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(configuration, false);
      }
    });
  }

  private setActive(configuration: ConfigurationItem, active: boolean): void {
    this.configService
      .update(configuration.id, { configValue: configuration.configValue, description: configuration.description, active })
      .subscribe({
        next: () => {
          this.snackBar.open(`Configuration ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', {
            duration: 3000
          });
          this.load();
        }
      });
  }
}
