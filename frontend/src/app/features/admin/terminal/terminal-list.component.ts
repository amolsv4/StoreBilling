import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { TerminalService } from '../../../core/services/terminal.service';
import { Terminal } from '../../../core/models/terminal.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { TerminalFormDialogComponent, TerminalFormDialogData } from './terminal-form-dialog.component';

@Component({
  selector: 'app-terminal-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './terminal-list.component.html'
})
export class TerminalListComponent {
  private readonly terminalService = inject(TerminalService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly terminals = signal<Terminal[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Terminal>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'storeCode', header: 'Store' },
    { key: 'terminalType', header: 'Type' },
    { key: 'deviceIdentifier', header: 'Device ID' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.terminalService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.terminals.set(page.content);
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

  openEdit(terminal: Terminal): void {
    this.openDialog({ mode: 'edit', terminal });
  }

  private openDialog(data: TerminalFormDialogData): void {
    this.dialog
      .open(TerminalFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(terminal: Terminal): void {
    if (!terminal.active) {
      this.setActive(terminal, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate terminal',
        message: `Deactivate "${terminal.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(terminal, false);
      }
    });
  }

  private setActive(terminal: Terminal, active: boolean): void {
    const { name, terminalType, deviceIdentifier } = terminal;
    this.terminalService.update(terminal.id, { name, terminalType, deviceIdentifier: deviceIdentifier ?? '', active }).subscribe({
      next: () => {
        this.snackBar.open(`Terminal ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
