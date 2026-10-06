import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrganizationService } from '../../../core/services/organization.service';
import { Organization } from '../../../core/models/organization.model';

export interface OrganizationFormDialogData {
  mode: 'create' | 'edit';
  organization?: Organization;
}

@Component({
  selector: 'app-organization-form-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatInputModule, MatButtonModule, MatSlideToggleModule],
  templateUrl: './organization-form-dialog.component.html'
})
export class OrganizationFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly organizationService = inject(OrganizationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<OrganizationFormDialogComponent, boolean>);
  // NOTE: injected as a plain field (not a constructor parameter property) - with
  // useDefineForClassFields, class field initializers run in declaration order before the
  // constructor body executes, so a parameter-property assignment to `data` would happen too late
  // for the `form`/`isEdit` field initializers below to read it.
  readonly data = inject(MAT_DIALOG_DATA) as OrganizationFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';

  readonly form = this.fb.nonNullable.group({
    code: [{ value: this.data.organization?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.organization?.name ?? '', [Validators.required]],
    legalName: [this.data.organization?.legalName ?? '', [Validators.required]],
    taxRegistrationNo: [this.data.organization?.taxRegistrationNo ?? ''],
    addressLine1: [this.data.organization?.addressLine1 ?? ''],
    addressLine2: [this.data.organization?.addressLine2 ?? ''],
    city: [this.data.organization?.city ?? ''],
    state: [this.data.organization?.state ?? ''],
    country: [this.data.organization?.country ?? ''],
    postalCode: [this.data.organization?.postalCode ?? ''],
    contactEmail: [this.data.organization?.contactEmail ?? '', [Validators.email]],
    contactPhone: [this.data.organization?.contactPhone ?? ''],
    active: [this.data.organization?.active ?? true]
  });

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const { code, ...updatable } = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.organizationService.update(this.data.organization!.id, updatable)
      : this.organizationService.create({ code, ...updatable });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Organization ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
