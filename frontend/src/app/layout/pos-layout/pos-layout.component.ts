import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { PosSessionService } from '../../core/services/pos-session.service';

/**
 * Slim top-level layout for the whole `/pos` area - no sidenav, just a toolbar showing the active
 * store/terminal plus Switch Terminal/Back to Admin/Logout. Navigating here unmounts
 * `ShellComponent` entirely (plan §5) since a cashier-facing screen has no use for the back-office
 * admin nav.
 */
@Component({
  selector: 'app-pos-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, MatToolbarModule, MatButtonModule, MatIconModule],
  templateUrl: './pos-layout.component.html',
  styleUrl: './pos-layout.component.scss'
})
export class PosLayoutComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  protected readonly posSessionService = inject(PosSessionService);

  readonly session = this.posSessionService.session;

  switchTerminal(): void {
    this.posSessionService.clear();
    this.router.navigate(['/pos/session']);
  }

  backToAdmin(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.authService.logout();
  }
}
