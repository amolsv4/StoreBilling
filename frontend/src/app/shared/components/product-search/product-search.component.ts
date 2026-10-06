import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, debounceTime, distinctUntilChanged, filter, map, of, switchMap } from 'rxjs';
import { Product } from '../../../core/models/product.model';
import { ProductService } from '../../../core/services/product.service';

/**
 * First real use of `MatAutocompleteModule` in this app (plan §5). Debounced search-as-you-type
 * against `GET /products?search=` (name/sku/barcode LIKE), plus an Enter-key exact-match fast path
 * against `GET /products/lookup` for barcode-scanner-style entry. Reused by the POS cart and the
 * three inventory header+lines screens (Purchase Receipt/Stock Transfer/Stock Take) - each supplies
 * its own `organizationId` and listens for `productSelected`.
 */
@Component({
  selector: 'app-product-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatAutocompleteModule, MatFormFieldModule, MatInputModule, MatIconModule],
  templateUrl: './product-search.component.html',
  styleUrl: './product-search.component.scss'
})
export class ProductSearchComponent {
  private readonly productService = inject(ProductService);

  @Input() organizationId: number | null = null;
  @Input() label = 'Product';
  @Input() placeholder = 'Scan or search by name, SKU, or barcode';
  @Input() disabled = false;

  @Output() productSelected = new EventEmitter<Product>();

  readonly options = signal<Product[]>([]);
  readonly searching = signal(false);

  readonly searchControl = new FormControl<string | Product>('', { nonNullable: true });

  readonly displayProduct = (value: string | Product): string => {
    if (value && typeof value === 'object') {
      return `${value.sku} - ${value.name}`;
    }
    return typeof value === 'string' ? value : '';
  };

  constructor() {
    this.searchControl.valueChanges
      .pipe(
        filter((value): value is string => typeof value === 'string'),
        map((value) => value.trim()),
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((term) => {
          if (!term || !this.organizationId) {
            return of<Product[]>([]);
          }
          this.searching.set(true);
          return this.productService.search(this.organizationId, term).pipe(
            map((page) => page.content),
            catchError(() => of<Product[]>([]))
          );
        })
      )
      .subscribe((products) => {
        this.searching.set(false);
        this.options.set(products);
      });
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    this.emitSelection(event.option.value as Product);
  }

  onEnter(event: Event): void {
    if (this.options().length > 0) {
      // Suggestions are already showing - let the cashier arrow-select/click one rather than
      // racing Material's own highlighted-option handling with a second exact-match lookup.
      return;
    }
    const raw = this.searchControl.value;
    const term = typeof raw === 'string' ? raw.trim() : '';
    if (!term || !this.organizationId) {
      return;
    }
    event.preventDefault();
    this.productService.lookup(this.organizationId, term).subscribe((product) => {
      if (product) {
        this.emitSelection(product);
      }
    });
  }

  private emitSelection(product: Product): void {
    this.productSelected.emit(product);
    this.options.set([]);
    // Reset synchronously (no emitEvent) so the field is immediately ready for the next scan
    // instead of lingering on the just-picked product's display string.
    this.searchControl.setValue('', { emitEvent: false });
  }
}
