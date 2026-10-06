import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { ProductPriceService } from '../../../core/services/product-price.service';
import { ProductPrice } from '../../../core/models/product-price.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ProductPriceFormDialogComponent, ProductPriceFormDialogData } from './product-price-form-dialog.component';

@Component({
  selector: 'app-product-price-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './product-price-list.component.html'
})
export class ProductPriceListComponent {
  private readonly productPriceService = inject(ProductPriceService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly productPrices = signal<ProductPrice[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<ProductPrice>[] = [
    { key: 'priceListName', header: 'Price List' },
    { key: 'productSku', header: 'Product SKU' },
    { key: 'productName', header: 'Product' },
    { key: 'basePrice', header: 'Base Price' },
    { key: 'sellingPrice', header: 'Selling Price' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.productPriceService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.productPrices.set(page.content);
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

  openEdit(productPrice: ProductPrice): void {
    this.openDialog({ mode: 'edit', productPrice });
  }

  private openDialog(data: ProductPriceFormDialogData): void {
    this.dialog
      .open(ProductPriceFormDialogComponent, { width: '640px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(productPrice: ProductPrice): void {
    if (!productPrice.active) {
      this.setActive(productPrice, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate product price',
        message: `Deactivate this price for "${productPrice.productName}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(productPrice, false);
      }
    });
  }

  private setActive(productPrice: ProductPrice, active: boolean): void {
    const { id, priceListId, productId, priceListCode, priceListName, productSku, productName, ...rest } =
      productPrice;
    this.productPriceService.update(id, { ...rest, active }).subscribe({
      next: () => {
        this.snackBar.open(`Product price ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', {
          duration: 3000
        });
        this.load();
      }
    });
  }
}
