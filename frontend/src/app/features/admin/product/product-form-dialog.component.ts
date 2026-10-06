import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { CategoryService } from '../../../core/services/category.service';
import { TaxGroupService } from '../../../core/services/tax-group.service';
import { Product, UOM_OPTIONS } from '../../../core/models/product.model';
import { Organization } from '../../../core/models/organization.model';
import { Category } from '../../../core/models/category.model';
import { TaxGroup } from '../../../core/models/tax-group.model';

export interface ProductFormDialogData {
  mode: 'create' | 'edit';
  product?: Product;
}

@Component({
  selector: 'app-product-form-dialog',
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
  templateUrl: './product-form-dialog.component.html'
})
export class ProductFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly organizationService = inject(OrganizationService);
  private readonly categoryService = inject(CategoryService);
  private readonly taxGroupService = inject(TaxGroupService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<ProductFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as ProductFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly uomOptions = UOM_OPTIONS;
  readonly organizations = signal<Organization[]>([]);
  private readonly allCategories = signal<Category[]>([]);
  readonly categoryOptions = signal<Category[]>([]);
  private readonly allTaxGroups = signal<TaxGroup[]>([]);
  readonly taxGroupOptions = signal<TaxGroup[]>([]);

  readonly form = this.fb.nonNullable.group({
    organizationId: [
      { value: this.data.product?.organizationId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    categoryId: [this.data.product?.categoryId ?? null, [Validators.required]],
    taxGroupId: [this.data.product?.taxGroupId ?? null],
    sku: [{ value: this.data.product?.sku ?? '', disabled: this.isEdit }, [Validators.required]],
    barcode: [this.data.product?.barcode ?? ''],
    name: [this.data.product?.name ?? '', [Validators.required]],
    description: [this.data.product?.description ?? ''],
    brand: [this.data.product?.brand ?? ''],
    uom: [this.data.product?.uom ?? 'PCS', [Validators.required]],
    saleEnabled: [this.data.product?.saleEnabled ?? true],
    purchaseEnabled: [this.data.product?.purchaseEnabled ?? true],
    active: [this.data.product?.active ?? true]
  });

  constructor() {
    this.organizationService.listAll().subscribe((orgs) => this.organizations.set(orgs));
    this.categoryService.listAll().subscribe((categories) => {
      this.allCategories.set(categories);
      this.refreshCategoryOptions(this.form.controls.organizationId.value);
    });
    this.taxGroupService.listAll().subscribe((taxGroups) => {
      this.allTaxGroups.set(taxGroups);
      this.refreshTaxGroupOptions(this.form.controls.organizationId.value);
    });

    this.form.controls.organizationId.valueChanges.subscribe((orgId) => {
      this.refreshCategoryOptions(orgId);
      this.refreshTaxGroupOptions(orgId);
      this.form.controls.categoryId.setValue(null);
      this.form.controls.taxGroupId.setValue(null);
    });
  }

  private refreshCategoryOptions(orgId: number | null): void {
    this.categoryOptions.set(orgId ? this.allCategories().filter((c) => c.organizationId === orgId) : []);
  }

  private refreshTaxGroupOptions(orgId: number | null): void {
    this.taxGroupOptions.set(orgId ? this.allTaxGroups().filter((g) => g.organizationId === orgId) : []);
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.productService.update(this.data.product!.id, {
          categoryId: raw.categoryId as number,
          taxGroupId: raw.taxGroupId,
          barcode: raw.barcode || null,
          name: raw.name,
          description: raw.description,
          brand: raw.brand,
          uom: raw.uom,
          saleEnabled: raw.saleEnabled,
          purchaseEnabled: raw.purchaseEnabled,
          active: raw.active
        })
      : this.productService.create({
          organizationId: raw.organizationId as number,
          categoryId: raw.categoryId as number,
          taxGroupId: raw.taxGroupId,
          sku: raw.sku,
          barcode: raw.barcode || null,
          name: raw.name,
          description: raw.description,
          brand: raw.brand,
          uom: raw.uom,
          saleEnabled: raw.saleEnabled,
          purchaseEnabled: raw.purchaseEnabled
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Product ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
