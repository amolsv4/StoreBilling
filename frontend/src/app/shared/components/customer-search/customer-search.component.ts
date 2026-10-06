import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Observable, debounceTime, distinctUntilChanged, filter, map, of, switchMap } from 'rxjs';
import { Customer } from '../../../core/models/customer.model';
import { CustomerService } from '../../../core/services/customer.service';

/**
 * Same shape as `ProductSearchComponent` (debounced autocomplete), adapted to the mobile-lookup
 * contract: `GET /customers/lookup?mobile=` is an *exact* match that can return several customers
 * (mobile isn't unique, e.g. a shared family phone) rather than a fuzzy name search - so this
 * debounces on the typed mobile number and lets the cashier pick among any matches. A "Walk-in"
 * button always clears the selection (plan §5).
 */
@Component({
  selector: 'app-customer-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './customer-search.component.html',
  styleUrl: './customer-search.component.scss'
})
export class CustomerSearchComponent {
  private readonly customerService = inject(CustomerService);

  @Input() organizationId: number | null = null;
  @Input() disabled = false;
  /** Currently selected customer's display name, shown next to the search field; `null` = walk-in. */
  @Input() selectedCustomerName: string | null = null;

  /** Emits the picked customer, or `null` when "Walk-in" is chosen. */
  @Output() customerSelected = new EventEmitter<Customer | null>();

  readonly matches = signal<Customer[]>([]);
  readonly searching = signal(false);

  readonly mobileControl = new FormControl<string>('', { nonNullable: true });

  constructor() {
    this.mobileControl.valueChanges
      .pipe(
        map((value) => value.trim()),
        filter((value) => value.length >= 4),
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((mobile): Observable<Customer[]> => {
          if (!this.organizationId) {
            return of<Customer[]>([]);
          }
          this.searching.set(true);
          return this.customerService.lookupByMobile(this.organizationId, mobile);
        })
      )
      .subscribe((customers) => {
        this.searching.set(false);
        this.matches.set(customers ?? []);
      });
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const customer = event.option.value as Customer;
    this.customerSelected.emit(customer);
    this.matches.set([]);
    this.mobileControl.setValue('', { emitEvent: false });
  }

  clearToWalkIn(): void {
    this.customerSelected.emit(null);
    this.matches.set([]);
    this.mobileControl.setValue('', { emitEvent: false });
  }
}
