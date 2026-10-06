import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SaleService } from '../../../core/services/sale.service';
import { CompleteSaleResponse, NOTIFICATION_CHANNELS, NotificationChannel } from '../../../core/models/sale.model';

export interface ReceiptDialogData {
  complete: CompleteSaleResponse;
}

/**
 * Invoice + lines + totals, a print button, and the three notification buttons (plan §5/§0.2). The
 * notification endpoint is a logging-only stub on the backend, but the UI flow is real - each
 * button independently posts `{ channel }` and reports success/failure via the standard envelope
 * (permission = same as viewing the sale, no special gate).
 */
@Component({
  selector: 'app-receipt-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './receipt-dialog.component.html',
  styleUrl: './receipt-dialog.component.scss'
})
export class ReceiptDialogComponent {
  private readonly saleService = inject(SaleService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<ReceiptDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA) as ReceiptDialogData;

  readonly complete = this.data.complete;
  /** The finalized sale from the complete response - it carries the invoice number the draft didn't have. */
  readonly sale = this.data.complete.sale;
  readonly notificationChannels: NotificationChannel[] = NOTIFICATION_CHANNELS;
  readonly sending = signal<NotificationChannel | null>(null);

  print(): void {
    window.print();
  }

  send(channel: NotificationChannel): void {
    this.sending.set(channel);
    this.saleService.sendNotification(this.sale.id, { channel }).subscribe({
      next: () => {
        this.sending.set(null);
        this.snackBar.open(`Receipt sent via ${channel}.`, 'Dismiss', { duration: 3000 });
      },
      error: () => this.sending.set(null)
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
