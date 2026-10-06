import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProductPriceService } from '../../../core/services/product-price.service';
import { PriceListService } from '../../../core/services/price-list.service';
import { ProductService } from '../../../core/services/product.service';
import { ProductPrice } from '../../../core/models/product-price.model';
import { PriceList } from '../../../core/models/price-list.model';
import { Product } from '../../../core/models/product.model';

export interface ProductPriceFormDialogData {
  mode: 'create' | 'edit';
  productPrice?: ProductPrice;
}

@Component({
  selector: 'app-product-price-form-dialog',
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
  templateUrl: './product-price-form-dialog.component.html'
})
export class ProductPriceFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly productPriceService = inject(ProductPriceService);
  private readonly priceListService = inject(PriceListService);
  private readonly productService = inject(ProductService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<ProductPriceFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as ProductPriceFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly priceLists = signal<PriceList[]>([]);
  readonly products = signal<Product[]>([]);

  readonly form = this.fb.nonNullable.group({
    priceListId: [
      { value: this.data.productPrice?.priceListId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    productId: [
      { value: this.data.productPrice?.productId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    basePrice: [this.data.productPrice?.basePrice ?? 0, [Validators.required, Validators.min(0)]],
    sellingPrice: [this.data.productPrice?.sellingPrice ?? 0, [Validators.required, Validators.min(0)]],
    effectiveFrom: [this.data.productPrice?.effectiveFrom ?? ''],
    effectiveTo: [this.data.productPrice?.effectiveTo ?? ''],
    active: [this.data.productPrice?.active ?? true]
  });

  constructor() {
    this.priceListService.listAll().subscribe((priceLists) => this.priceLists.set(priceLists));
    this.productService.listAll().subscribe((products) => this.products.set(products));
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.productPriceService.update(this.data.productPrice!.id, {
          basePrice: raw.basePrice,
          sellingPrice: raw.sellingPrice,
          effectiveFrom: raw.effectiveFrom || null,
          effectiveTo: raw.effectiveTo || null,
          active: raw.active
        })
      : this.productPriceService.create({
          priceListId: raw.priceListId as number,
          productId: raw.productId as number,
          basePrice: raw.basePrice,
          sellingPrice: raw.sellingPrice,
          effectiveFrom: raw.effectiveFrom || null,
          effectiveTo: raw.effectiveTo || null
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Product price ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
