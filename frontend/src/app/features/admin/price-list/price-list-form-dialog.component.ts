import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PriceListService } from '../../../core/services/price-list.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { PriceList } from '../../../core/models/price-list.model';
import { Organization } from '../../../core/models/organization.model';

export interface PriceListFormDialogData {
  mode: 'create' | 'edit';
  priceList?: PriceList;
}

@Component({
  selector: 'app-price-list-form-dialog',
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
  templateUrl: './price-list-form-dialog.component.html'
})
export class PriceListFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly priceListService = inject(PriceListService);
  private readonly organizationService = inject(OrganizationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<PriceListFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as PriceListFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly organizations = signal<Organization[]>([]);

  readonly form = this.fb.nonNullable.group({
    organizationId: [
      { value: this.data.priceList?.organizationId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    code: [{ value: this.data.priceList?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.priceList?.name ?? '', [Validators.required]],
    description: [this.data.priceList?.description ?? ''],
    effectiveFrom: [this.data.priceList?.effectiveFrom ?? ''],
    effectiveTo: [this.data.priceList?.effectiveTo ?? ''],
    active: [this.data.priceList?.active ?? true]
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
    const effectiveFrom = raw.effectiveFrom || null;
    const effectiveTo = raw.effectiveTo || null;

    const request$ = this.isEdit
      ? this.priceListService.update(this.data.priceList!.id, {
          name: raw.name,
          description: raw.description,
          effectiveFrom,
          effectiveTo,
          active: raw.active
        })
      : this.priceListService.create({
          organizationId: raw.organizationId as number,
          code: raw.code,
          name: raw.name,
          description: raw.description,
          effectiveFrom,
          effectiveTo
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Price list ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
