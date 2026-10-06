import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomerService } from '../../../core/services/customer.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { Customer } from '../../../core/models/customer.model';
import { Organization } from '../../../core/models/organization.model';

export interface CustomerFormDialogData {
  mode: 'create' | 'edit';
  customer?: Customer;
}

@Component({
  selector: 'app-customer-form-dialog',
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
  templateUrl: './customer-form-dialog.component.html'
})
export class CustomerFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);
  private readonly organizationService = inject(OrganizationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<CustomerFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as CustomerFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  /** Response-only accrued value; shown read-only while editing, never sent in the request. */
  readonly loyaltyPoints = this.data.customer?.loyaltyPoints ?? 0;
  readonly organizations = signal<Organization[]>([]);

  readonly form = this.fb.nonNullable.group({
    organizationId: [
      { value: this.data.customer?.organizationId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    code: [{ value: this.data.customer?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.customer?.name ?? '', [Validators.required]],
    mobile: [this.data.customer?.mobile ?? ''],
    email: [this.data.customer?.email ?? '', [Validators.email]],
    addressLine1: [this.data.customer?.addressLine1 ?? ''],
    addressLine2: [this.data.customer?.addressLine2 ?? ''],
    city: [this.data.customer?.city ?? ''],
    state: [this.data.customer?.state ?? ''],
    country: [this.data.customer?.country ?? ''],
    postalCode: [this.data.customer?.postalCode ?? ''],
    taxRegistrationNo: [this.data.customer?.taxRegistrationNo ?? ''],
    creditLimit: [this.data.customer?.creditLimit ?? 0, [Validators.min(0)]],
    active: [this.data.customer?.active ?? true]
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
      ? this.customerService.update(this.data.customer!.id, {
          name: raw.name,
          mobile: raw.mobile,
          email: raw.email,
          addressLine1: raw.addressLine1,
          addressLine2: raw.addressLine2,
          city: raw.city,
          state: raw.state,
          country: raw.country,
          postalCode: raw.postalCode,
          taxRegistrationNo: raw.taxRegistrationNo,
          creditLimit: raw.creditLimit,
          active: raw.active
        })
      : this.customerService.create({
          organizationId: raw.organizationId as number,
          code: raw.code,
          name: raw.name,
          mobile: raw.mobile,
          email: raw.email,
          addressLine1: raw.addressLine1,
          addressLine2: raw.addressLine2,
          city: raw.city,
          state: raw.state,
          country: raw.country,
          postalCode: raw.postalCode,
          taxRegistrationNo: raw.taxRegistrationNo,
          creditLimit: raw.creditLimit
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Customer ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
