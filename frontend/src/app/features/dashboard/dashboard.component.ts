import { Component, inject } from '@angular/core';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { AuthService } from '../../core/services/auth.service';

/** Minimal placeholder - later phases build real widgets here. */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [PageHeaderComponent],
  template: `
    <app-page-header title="Dashboard"></app-page-header>
    <p>Welcome{{ authService.currentUser()?.username ? ', ' + authService.currentUser()?.username : '' }}.</p>
  `
})
export class DashboardComponent {
  protected readonly authService = inject(AuthService);
}
