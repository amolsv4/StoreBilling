import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/** Thin wrapper around MatProgressSpinner with a centered layout and optional label. */
@Component({
  selector: 'app-loading-indicator',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="loading-indicator" *ngIf="show">
      <mat-spinner [diameter]="diameter"></mat-spinner>
      <span *ngIf="label" class="loading-label">{{ label }}</span>
    </div>
  `,
  styles: [
    `
      .loading-indicator {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;
        padding: 24px;
      }
      .loading-label {
        color: rgba(0, 0, 0, 0.6);
        font-size: 14px;
      }
    `
  ]
})
export class LoadingIndicatorComponent {
  @Input() show = true;
  @Input() diameter = 40;
  @Input() label = '';
}
