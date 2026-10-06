import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { StoreService } from '../../../core/services/store.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { BusinessTypeService } from '../../../core/services/business-type.service';
import { PriceListService } from '../../../core/services/price-list.service';
import { Store } from '../../../core/models/store.model';
import { Organization } from '../../../core/models/organization.model';
import { BusinessType } from '../../../core/models/business-type.model';
import { PriceList } from '../../../core/models/price-list.model';

export interface StoreFormDialogData {
  mode: 'create' | 'edit';
  store?: Store;
}

@Component({
  selector: 'app-store-form-dialog',
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
  templateUrl: './store-form-dialog.component.html'
})
export class StoreFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly storeService = inject(StoreService);
  private readonly organizationService = inject(OrganizationService);
  private readonly businessTypeService = inject(BusinessTypeService);
  private readonly priceListService = inject(PriceListService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<StoreFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as StoreFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly organizations = signal<Organization[]>([]);
  readonly businessTypes = signal<BusinessType[]>([]);
  readonly priceLists = signal<PriceList[]>([]);

  readonly form = this.fb.nonNullable.group({
    // Organization, business type and code are fixed at creation (UpdateStoreRequest omits them).
    organizationId: [{ value: this.data.store?.organizationId ?? null, disabled: this.isEdit }, [Validators.required]],
    businessTypeId: [{ value: this.data.store?.businessTypeId ?? null, disabled: this.isEdit }, [Validators.required]],
    code: [{ value: this.data.store?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.store?.name ?? '', [Validators.required]],
    timezone: [this.data.store?.timezone ?? 'UTC', [Validators.required]],
    addressLine1: [this.data.store?.addressLine1 ?? ''],
    addressLine2: [this.data.store?.addressLine2 ?? ''],
    city: [this.data.store?.city ?? ''],
    state: [this.data.store?.state ?? ''],
    country: [this.data.store?.country ?? ''],
    postalCode: [this.data.store?.postalCode ?? ''],
    email: [this.data.store?.email ?? '', [Validators.email]],
    phone: [this.data.store?.phone ?? ''],
    active: [this.data.store?.active ?? true],
    defaultPriceListId: [this.data.store?.defaultPriceListId ?? (null as number | null)]
  });

  constructor() {
    this.organizationService.listAll().subscribe((orgs) => this.organizations.set(orgs));
    this.businessTypeService.listAll().subscribe((types) => this.businessTypes.set(types));
    if (this.isEdit) {
      // Only the store's own organization's price lists can sensibly be its default.
      this.priceListService
        .listAll()
        .subscribe((lists) => this.priceLists.set(lists.filter((l) => l.organizationId === this.data.store!.organizationId)));
    }
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();
    const contact = {
      timezone: raw.timezone,
      addressLine1: raw.addressLine1,
      addressLine2: raw.addressLine2,
      city: raw.city,
      state: raw.state,
      country: raw.country,
      postalCode: raw.postalCode,
      phone: raw.phone,
      email: raw.email
    };

    const request$ = this.isEdit
      ? this.storeService.update(this.data.store!.id, {
          ...contact,
          name: raw.name,
          active: raw.active,
          defaultPriceListId: raw.defaultPriceListId
        })
      : this.storeService.create({
          ...contact,
          organizationId: raw.organizationId as number,
          businessTypeId: raw.businessTypeId as number,
          code: raw.code,
          name: raw.name
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Store ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => this.submitting.set(false)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
