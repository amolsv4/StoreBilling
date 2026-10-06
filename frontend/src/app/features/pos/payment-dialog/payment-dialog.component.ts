import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { SaleService } from '../../../core/services/sale.service';
import { PAYMENT_METHODS, PaymentMethod, PaymentResponse, SaleResponse } from '../../../core/models/sale.model';
import { ReceiptDialogComponent, ReceiptDialogData } from '../receipt-dialog/receipt-dialog.component';

export interface PaymentDialogData {
  sale: SaleResponse;
}

export interface PaymentDialogResult {
  outcome: 'completed' | 'held' | 'cancelled';
}

/**
 * Add-tender form with a running remaining-balance/change-due (plan §5). Tenders post
 * incrementally (`POST /sales/{id}/payments`); `complete()` is called only once the remaining
 * balance is zero, and is safe to retry (reconciliation note 5). On any failed payment POST, the
 * dialog re-fetches the sale + its payments from the server rather than trusting local optimistic
 * state, then lets the cashier retry. Once any money has been tendered, "Cancel" becomes
 * "Hold Instead" so a partially-paid sale is never orphaned outside the Held Bills list.
 */
@Component({
  selector: 'app-payment-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatListModule
  ],
  templateUrl: './payment-dialog.component.html',
  styleUrl: './payment-dialog.component.scss'
})
export class PaymentDialogComponent {
  private readonly saleService = inject(SaleService);
  private readonly dialog = inject(MatDialog);
  private readonly dialogRef = inject(MatDialogRef<PaymentDialogComponent, PaymentDialogResult>);
  // Declared before the fields below that read it, matching the MAT_DIALOG_DATA-as-field ordering
  // convention used across every other dialog in this app.
  readonly data = inject(MAT_DIALOG_DATA) as PaymentDialogData;

  readonly sale = signal<SaleResponse>(this.data.sale);
  readonly payments = signal<PaymentResponse[]>([]);
  readonly submitting = signal(false);
  readonly loadingPayments = signal(true);

  readonly paymentMethods: PaymentMethod[] = PAYMENT_METHODS;
  selectedMethod: PaymentMethod = 'CASH';
  amountInput = 0;
  referenceInput = '';

  readonly tenderedAny = computed(() => this.payments().length > 0);
  readonly totalDue = computed(() => this.sale().totalAmount);
  readonly amountPaid = computed(() => this.payments().reduce((sum, p) => sum + p.amount, 0));
  readonly remainingBalance = computed(() => Math.max(0, round2(this.totalDue() - this.amountPaid())));
  readonly changeDue = computed(() => Math.max(0, round2(this.amountPaid() - this.totalDue())));

  constructor() {
    this.loadPayments();
  }

  addTender(): void {
    if (this.amountInput <= 0 || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.saleService
      .addPayment(this.sale().id, {
        paymentMethod: this.selectedMethod,
        amount: this.amountInput,
        referenceNumber: this.referenceInput || null
      })
      .subscribe({
        next: (payment) => {
          this.payments.update((list) => [...list, payment]);
          this.amountInput = this.remainingBalance();
          this.referenceInput = '';
          this.submitting.set(false);
        },
        error: () => {
          this.submitting.set(false);
          this.refetchAfterFailure();
        }
      });
  }

  completeSale(): void {
    if (this.remainingBalance() > 0 || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.saleService.complete(this.sale().id).subscribe({
      next: (result) => {
        this.submitting.set(false);
        const receiptData: ReceiptDialogData = { complete: result };
        this.dialog.open(ReceiptDialogComponent, { width: '520px', data: receiptData });
        this.dialogRef.close({ outcome: 'completed' });
      },
      error: () => {
        // complete() is safe to retry (reconciliation note 5) - re-fetch and let the cashier press
        // Complete Sale again rather than assuming the failed response means nothing happened.
        this.submitting.set(false);
        this.refetchAfterFailure();
      }
    });
  }

  cancelOrHold(): void {
    if (this.tenderedAny()) {
      this.submitting.set(true);
      this.saleService.hold(this.sale().id).subscribe({
        next: () => {
          this.submitting.set(false);
          this.dialogRef.close({ outcome: 'held' });
        },
        error: () => this.submitting.set(false)
      });
      return;
    }
    this.dialogRef.close({ outcome: 'cancelled' });
  }

  private loadPayments(): void {
    this.loadingPayments.set(true);
    this.saleService.listPayments(this.sale().id).subscribe({
      next: (payments) => {
        this.payments.set(payments);
        this.amountInput = this.remainingBalance();
        this.loadingPayments.set(false);
      },
      error: () => this.loadingPayments.set(false)
    });
  }

  /** Never trust local optimistic state after a failed payment/complete POST - reload both the
   *  sale and its payments from the server before letting the cashier retry. */
  private refetchAfterFailure(): void {
    this.saleService.getById(this.sale().id).subscribe((refreshed) => this.sale.set(refreshed));
    this.loadPayments();
  }
}

function round2(value: number): number {
  return Math.round(value * 100) / 100;
}
