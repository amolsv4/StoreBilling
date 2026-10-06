import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, map } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfigService } from '../../../core/services/config.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { StoreService } from '../../../core/services/store.service';
import { TerminalService } from '../../../core/services/terminal.service';
import { BusinessTypeService } from '../../../core/services/business-type.service';
import {
  CONFIG_DATA_TYPES,
  ConfigDataType,
  ConfigurationItem,
  SCOPE_TYPES,
  ScopeType
} from '../../../core/models/configuration.model';

export interface ConfigurationFormDialogData {
  mode: 'create' | 'edit';
  configuration?: ConfigurationItem;
}

interface ScopeOption {
  id: number;
  label: string;
}

/**
 * Create mode collects the full POST /config shape (configKey, configValue, dataType, scopeType,
 * scopeId, description). Edit mode only ever submits PUT /config/{id}'s allowed subset
 * (configValue, description, active) - configKey/scopeType/scopeId/dataType are immutable after
 * creation per the contract, so they are rendered as disabled/read-only in edit mode.
 */
@Component({
  selector: 'app-configuration-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSlideToggleModule
  ],
  templateUrl: './configuration-form-dialog.component.html'
})
export class ConfigurationFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly configService = inject(ConfigService);
  private readonly organizationService = inject(OrganizationService);
  private readonly storeService = inject(StoreService);
  private readonly terminalService = inject(TerminalService);
  private readonly businessTypeService = inject(BusinessTypeService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<ConfigurationFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as ConfigurationFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly scopeTypes = SCOPE_TYPES;
  readonly dataTypes = CONFIG_DATA_TYPES;
  readonly scopeOptions = signal<ScopeOption[]>([]);

  readonly form = this.fb.nonNullable.group({
    configKey: [{ value: this.data.configuration?.configKey ?? '', disabled: this.isEdit }, [Validators.required]],
    scopeType: [
      { value: this.data.configuration?.scopeType ?? ('GLOBAL' as ScopeType), disabled: this.isEdit },
      [Validators.required]
    ],
    scopeId: [{ value: this.data.configuration?.scopeId ?? null, disabled: this.isEdit }],
    dataType: [
      { value: (this.data.configuration?.dataType ?? 'STRING') as ConfigDataType, disabled: this.isEdit },
      [Validators.required]
    ],
    configValue: [this.data.configuration?.configValue ?? '', [Validators.required]],
    description: [this.data.configuration?.description ?? ''],
    active: [this.data.configuration?.active ?? true]
  });

  constructor() {
    if (!this.isEdit) {
      this.form.controls.scopeType.valueChanges.subscribe((scopeType) => this.onScopeTypeChange(scopeType));
      this.onScopeTypeChange(this.form.controls.scopeType.value);
    }
  }

  get requiresScopeId(): boolean {
    return this.form.controls.scopeType.value !== 'GLOBAL';
  }

  /** Human-readable scope label for the disabled read-only field shown in edit mode. */
  get editScopeLabel(): string {
    const config = this.data.configuration;
    if (!config || config.scopeType === 'GLOBAL') {
      return 'GLOBAL';
    }
    return config.scopeName ? `${config.scopeType} - ${config.scopeName}` : `${config.scopeType} #${config.scopeId}`;
  }

  private onScopeTypeChange(scopeType: ScopeType): void {
    this.form.controls.scopeId.setValue(null);
    if (scopeType === 'GLOBAL') {
      this.scopeOptions.set([]);
      return;
    }

    const toOptions = (items: Array<{ id: number; name: string }>): ScopeOption[] =>
      items.map((item) => ({ id: item.id, label: item.name }));

    const source$: Observable<ScopeOption[]> =
      scopeType === 'ORGANIZATION'
        ? this.organizationService.listAll().pipe(map(toOptions))
        : scopeType === 'STORE'
        ? this.storeService.listAll().pipe(map(toOptions))
        : scopeType === 'TERMINAL'
        ? this.terminalService.listAll().pipe(map(toOptions))
        : this.businessTypeService.listAll().pipe(map(toOptions));

    source$.subscribe((options) => this.scopeOptions.set(options));
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.isEdit && this.requiresScopeId && this.form.controls.scopeId.value == null) {
      this.form.controls.scopeId.setErrors({ required: true });
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.configService.update(this.data.configuration!.id, {
          configValue: raw.configValue,
          description: raw.description || null,
          active: raw.active
        })
      : this.configService.create({
          configKey: raw.configKey,
          configValue: raw.configValue,
          dataType: raw.dataType,
          scopeType: raw.scopeType,
          scopeId: raw.scopeType === 'GLOBAL' ? null : raw.scopeId,
          description: raw.description || null
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Configuration ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
          duration: 3000
        });
        this.dialogRef.close(true);
      },
      error: () => this.submitting.set(false)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
