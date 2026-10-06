import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { PosSessionService } from '../../../core/services/pos-session.service';
import { Store } from '../../../core/models/store.model';
import { Terminal } from '../../../core/models/terminal.model';

/** Enforces a store/terminal choice before `/pos/sale` or `/pos/held-bills` becomes usable
 *  (plan §5, acceptance criterion 8). Reached directly, or via `posSessionGuard` redirecting here. */
@Component({
  selector: 'app-pos-session-picker',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatSelectModule, MatButtonModule, MatIconModule],
  templateUrl: './pos-session-picker.component.html',
  styleUrl: './pos-session-picker.component.scss'
})
export class PosSessionPickerComponent {
  private readonly posSessionService = inject(PosSessionService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loadingStores = signal(true);
  readonly loadingTerminals = signal(false);
  readonly stores = signal<Store[]>([]);
  readonly terminals = signal<Terminal[]>([]);

  selectedStoreId: number | null = null;
  selectedTerminalId: number | null = null;

  constructor() {
    this.posSessionService.accessibleStores().subscribe((stores) => {
      this.stores.set(stores);
      this.loadingStores.set(false);
    });
  }

  onStoreChange(storeId: number): void {
    this.selectedStoreId = storeId;
    this.selectedTerminalId = null;
    this.terminals.set([]);
    this.loadingTerminals.set(true);
    this.posSessionService.terminalsForStore(storeId).subscribe((terminals) => {
      this.terminals.set(terminals);
      this.loadingTerminals.set(false);
    });
  }

  startSession(): void {
    const store = this.stores().find((s) => s.id === this.selectedStoreId);
    const terminal = this.terminals().find((t) => t.id === this.selectedTerminalId);
    if (!store || !terminal) {
      return;
    }
    this.posSessionService.setSession(store, terminal);
    this.snackBar.open(`Session started at ${store.name} / ${terminal.name}.`, 'Dismiss', { duration: 3000 });
    this.router.navigate(['/pos/sale']);
  }
}
