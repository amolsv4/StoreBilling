import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ConfigService } from '../../../core/services/config.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { StoreService } from '../../../core/services/store.service';
import { TerminalService } from '../../../core/services/terminal.service';
import { BusinessTypeService } from '../../../core/services/business-type.service';
import { ResolvedConfig } from '../../../core/models/configuration.model';
import { Organization } from '../../../core/models/organization.model';
import { Store } from '../../../core/models/store.model';
import { Terminal } from '../../../core/models/terminal.model';
import { BusinessType } from '../../../core/models/business-type.model';

/**
 * Ad-hoc "resolve" panel: pick a key plus optional terminal/store/organization/business-type
 * context and show what GET /config/{key} resolves to and which scope it came from. This is the
 * screen that visibly demonstrates the configuration engine's scope-precedence behavior.
 */
@Component({
  selector: 'app-configuration-resolve-panel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatInputModule, MatSelectModule],
  templateUrl: './configuration-resolve-panel.component.html'
})
export class ConfigurationResolvePanelComponent {
  private readonly fb = inject(FormBuilder);
  private readonly configService = inject(ConfigService);
  private readonly organizationService = inject(OrganizationService);
  private readonly storeService = inject(StoreService);
  private readonly terminalService = inject(TerminalService);
  private readonly businessTypeService = inject(BusinessTypeService);

  readonly organizations = signal<Organization[]>([]);
  readonly stores = signal<Store[]>([]);
  readonly terminals = signal<Terminal[]>([]);
  readonly businessTypes = signal<BusinessType[]>([]);

  readonly submitting = signal(false);
  readonly result = signal<ResolvedConfig | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    key: ['', [Validators.required]],
    organizationId: [null as number | null],
    businessTypeId: [null as number | null],
    storeId: [null as number | null],
    terminalId: [null as number | null]
  });

  constructor() {
    this.organizationService.listAll().subscribe((v) => this.organizations.set(v));
    this.storeService.listAll().subscribe((v) => this.stores.set(v));
    this.terminalService.listAll().subscribe((v) => this.terminals.set(v));
    this.businessTypeService.listAll().subscribe((v) => this.businessTypes.set(v));
  }

  resolve(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.result.set(null);

    const { key, organizationId, businessTypeId, storeId, terminalId } = this.form.getRawValue();
    this.configService.resolve(key, { organizationId, businessTypeId, storeId, terminalId }).subscribe({
      next: (resolved) => {
        this.submitting.set(false);
        this.result.set(resolved);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not resolve this key for the given scope context.');
      }
    });
  }
}
