import { Component } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { ConfigurationListComponent } from './configuration-list.component';
import { ConfigurationResolvePanelComponent } from './configuration-resolve-panel.component';

/**
 * Routed entry point for /admin/configuration. Hosts two tabs: the admin CRUD list (create/list/
 * update, active toggle) and the ad-hoc "resolve" panel that demonstrates the configuration
 * engine's scope resolution.
 */
@Component({
  selector: 'app-configuration-page',
  standalone: true,
  imports: [MatTabsModule, ConfigurationListComponent, ConfigurationResolvePanelComponent],
  template: `
    <mat-tab-group>
      <mat-tab label="Configurations">
        <div class="tab-body">
          <app-configuration-list></app-configuration-list>
        </div>
      </mat-tab>
      <mat-tab label="Resolve">
        <div class="tab-body">
          <app-configuration-resolve-panel></app-configuration-resolve-panel>
        </div>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: [
    `
      .tab-body {
        padding-top: 16px;
      }
    `
  ]
})
export class ConfigurationPageComponent {}
