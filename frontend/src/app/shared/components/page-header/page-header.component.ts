import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/** Title + right-aligned action button slot (projected content), used atop every feature page. */
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <div class="page-header-text">
        <h1>{{ title }}</h1>
        <p *ngIf="subtitle">{{ subtitle }}</p>
      </div>
      <div class="page-header-actions">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [
    `
      .page-header {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 16px;
        flex-wrap: wrap;
      }
      .page-header-text h1 {
        margin: 0 0 4px 0;
        font-size: 22px;
        font-weight: 500;
      }
      .page-header-text p {
        margin: 0;
        color: rgba(0, 0, 0, 0.6);
        font-size: 14px;
      }
      .page-header-actions {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    `
  ]
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
}
