import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RoleService } from '../../../core/services/role.service';
import { Role } from '../../../core/models/role.model';
import { PERMISSION_CODES } from '../../../core/constants/permission-codes';

export interface RoleFormDialogData {
  mode: 'create' | 'edit';
  role?: Role;
}

@Component({
  selector: 'app-role-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './role-form-dialog.component.html'
})
export class RoleFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly roleService = inject(RoleService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<RoleFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as RoleFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  /** See core/constants/permission-codes.ts for why this is a hardcoded catalog. */
  readonly availablePermissionCodes = PERMISSION_CODES;

  readonly form = this.fb.nonNullable.group({
    code: [{ value: this.data.role?.code ?? '', disabled: this.data.mode === 'edit' }, [Validators.required]],
    name: [this.data.role?.name ?? '', [Validators.required]],
    description: [this.data.role?.description ?? ''],
    permissionCodes: [this.data.role?.permissions ?? ([] as string[])]
  });

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const value = this.form.getRawValue();

    // The role code is immutable (UpdateRoleRequest has no `code`), so only create sends it.
    const { code, ...updatable } = value;
    const request$ = this.isEdit
      ? this.roleService.update(this.data.role!.id, updatable)
      : this.roleService.create(value);

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Role ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => this.submitting.set(false)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
