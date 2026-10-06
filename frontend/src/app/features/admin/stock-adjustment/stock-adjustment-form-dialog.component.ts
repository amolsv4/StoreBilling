import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { StockAdjustmentService } from '../../../core/services/stock-adjustment.service';
import { StoreService } from '../../../core/services/store.service';
import { STOCK_ADJUSTMENT_TYPES, StockAdjustmentType } from '../../../core/models/stock-adjustment.model';
import { Store } from '../../../core/models/store.model';
import { Product } from '../../../core/models/product.model';
import { ProductSearchComponent } from '../../../shared/components/product-search/product-search.component';

/**
 * Create-only, ordinary Phase 2 list+dialog pattern (plan §5) - single product pick via
 * `ProductSearchComponent`, no lines, no update, so there's no `MAT_DIALOG_DATA` mode/entity to
 * inject at all.
 */
@Component({
  selector: 'app-stock-adjustment-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ProductSearchComponent
  ],
  templateUrl: './stock-adjustment-form-dialog.component.html'
})
export class StockAdjustmentFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly stockAdjustmentService = inject(StockAdjustmentService);
  private readonly storeService = inject(StoreService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<StockAdjustmentFormDialogComponent, boolean>);

  readonly organizationId = this.authService.currentUser()?.organizationId ?? null;
  readonly submitting = signal(false);
  readonly stores = signal<Store[]>([]);
  readonly selectedProduct = signal<Product | null>(null);
  readonly adjustmentTypes: StockAdjustmentType[] = STOCK_ADJUSTMENT_TYPES;

  readonly form = this.fb.nonNullable.group({
    storeId: [null as number | null, [Validators.required]],
    adjustmentType: ['ADJUSTMENT' as StockAdjustmentType, [Validators.required]],
    quantityChange: [0, [Validators.required]],
    reason: ['', [Validators.required]]
  });

  constructor() {
    this.storeService.listAll().subscribe((stores) => this.stores.set(stores));
  }

  onProductSelected(product: Product): void {
    this.selectedProduct.set(product);
  }

  submit(): void {
    const product = this.selectedProduct();
    if (this.form.invalid || !product || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();
    this.stockAdjustmentService
      .create({
        storeId: raw.storeId as number,
        productId: product.id,
        adjustmentType: raw.adjustmentType,
        quantityChange: raw.quantityChange,
        reason: raw.reason
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.snackBar.open('Stock adjustment recorded.', 'Dismiss', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: () => this.submitting.set(false)
      });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
