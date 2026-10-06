import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TerminalService } from '../../../core/services/terminal.service';
import { StoreService } from '../../../core/services/store.service';
import { Terminal, TERMINAL_TYPES } from '../../../core/models/terminal.model';
import { Store } from '../../../core/models/store.model';

export interface TerminalFormDialogData {
  mode: 'create' | 'edit';
  terminal?: Terminal;
}

@Component({
  selector: 'app-terminal-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSlideToggleModule
  ],
  templateUrl: './terminal-form-dialog.component.html'
})
export class TerminalFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly terminalService = inject(TerminalService);
  private readonly storeService = inject(StoreService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<TerminalFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as TerminalFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly stores = signal<Store[]>([]);
  readonly terminalTypes = TERMINAL_TYPES;

  readonly form = this.fb.nonNullable.group({
    // The store and code are fixed at creation (UpdateTerminalRequest omits them).
    storeId: [{ value: this.data.terminal?.storeId ?? null, disabled: this.isEdit }, [Validators.required]],
    code: [{ value: this.data.terminal?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.terminal?.name ?? '', [Validators.required]],
    terminalType: [this.data.terminal?.terminalType ?? 'POS', [Validators.required]],
    deviceIdentifier: [this.data.terminal?.deviceIdentifier ?? ''],
    active: [this.data.terminal?.active ?? true]
  });

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();
    const request$ = this.isEdit
      ? this.terminalService.update(this.data.terminal!.id, {
          name: raw.name,
          terminalType: raw.terminalType,
          deviceIdentifier: raw.deviceIdentifier,
          active: raw.active
        })
      : this.terminalService.create({
          storeId: raw.storeId as number,
          code: raw.code,
          name: raw.name,
          terminalType: raw.terminalType,
          deviceIdentifier: raw.deviceIdentifier
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Terminal ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
          duration: 3000
        });
        this.dialogRef.close(true);
      },
      error: () => this.submitting.set(false)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
