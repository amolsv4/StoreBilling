import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ProductFormDialogComponent, ProductFormDialogData } from './product-form-dialog.component';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './product-list.component.html'
})
export class ProductListComponent {
  private readonly productService = inject(ProductService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly products = signal<Product[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Product>[] = [
    { key: 'sku', header: 'SKU' },
    { key: 'name', header: 'Name' },
    { key: 'organizationName', header: 'Organization' },
    { key: 'categoryName', header: 'Category' },
    { key: 'taxGroupName', header: 'Tax Group' },
    { key: 'uom', header: 'UOM' },
    { key: 'saleEnabled', header: 'Sale', isBoolean: true },
    { key: 'purchaseEnabled', header: 'Purchase', isBoolean: true }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.productService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.products.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openCreate(): void {
    this.openDialog({ mode: 'create' });
  }

  openEdit(product: Product): void {
    this.openDialog({ mode: 'edit', product });
  }

  private openDialog(data: ProductFormDialogData): void {
    this.dialog
      .open(ProductFormDialogComponent, { width: '700px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(product: Product): void {
    if (!product.active) {
      this.setActive(product, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate product',
        message: `Deactivate "${product.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(product, false);
      }
    });
  }

  private setActive(product: Product, active: boolean): void {
    const {
      id,
      organizationId,
      sku,
      organizationCode,
      organizationName,
      categoryCode,
      categoryName,
      taxGroupCode,
      taxGroupName,
      ...rest
    } = product;
    this.productService.update(id, { ...rest, active }).subscribe({
      next: () => {
        this.snackBar.open(`Product ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
