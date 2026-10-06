import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TaxComponentService } from '../../../core/services/tax-component.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { TaxComponent, TAX_TYPES } from '../../../core/models/tax-component.model';
import { Organization } from '../../../core/models/organization.model';

export interface TaxComponentFormDialogData {
  mode: 'create' | 'edit';
  taxComponent?: TaxComponent;
}

@Component({
  selector: 'app-tax-component-form-dialog',
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
  templateUrl: './tax-component-form-dialog.component.html'
})
export class TaxComponentFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly taxComponentService = inject(TaxComponentService);
  private readonly organizationService = inject(OrganizationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<TaxComponentFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as TaxComponentFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly organizations = signal<Organization[]>([]);
  readonly taxTypes = TAX_TYPES;

  readonly form = this.fb.nonNullable.group({
    organizationId: [
      { value: this.data.taxComponent?.organizationId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    code: [{ value: this.data.taxComponent?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.taxComponent?.name ?? '', [Validators.required]],
    taxType: [this.data.taxComponent?.taxType ?? 'GENERAL', [Validators.required]],
    percentage: [
      this.data.taxComponent?.percentage ?? 0,
      [Validators.required, Validators.min(0), Validators.max(100)]
    ],
    active: [this.data.taxComponent?.active ?? true]
  });

  constructor() {
    this.organizationService.listAll().subscribe((orgs) => this.organizations.set(orgs));
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.taxComponentService.update(this.data.taxComponent!.id, {
          name: raw.name,
          taxType: raw.taxType,
          percentage: raw.percentage,
          active: raw.active
        })
      : this.taxComponentService.create({
          organizationId: raw.organizationId as number,
          code: raw.code,
          name: raw.name,
          taxType: raw.taxType,
          percentage: raw.percentage
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Tax component ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
