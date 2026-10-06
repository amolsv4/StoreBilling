import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TaxGroupService } from '../../../core/services/tax-group.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { TaxComponentService } from '../../../core/services/tax-component.service';
import { TaxGroup } from '../../../core/models/tax-group.model';
import { Organization } from '../../../core/models/organization.model';
import { TaxComponent } from '../../../core/models/tax-component.model';

export interface TaxGroupFormDialogData {
  mode: 'create' | 'edit';
  taxGroup?: TaxGroup;
}

@Component({
  selector: 'app-tax-group-form-dialog',
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
  templateUrl: './tax-group-form-dialog.component.html'
})
export class TaxGroupFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly taxGroupService = inject(TaxGroupService);
  private readonly organizationService = inject(OrganizationService);
  private readonly taxComponentService = inject(TaxComponentService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<TaxGroupFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as TaxGroupFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  /** Read-only display shown while editing; never sent back in the request (see tax-group.model.ts). */
  readonly totalPercentage = this.data.taxGroup?.totalPercentage ?? 0;
  readonly organizations = signal<Organization[]>([]);
  private readonly allTaxComponents = signal<TaxComponent[]>([]);
  readonly taxComponentOptions = signal<TaxComponent[]>([]);

  readonly form = this.fb.nonNullable.group({
    organizationId: [
      { value: this.data.taxGroup?.organizationId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    code: [{ value: this.data.taxGroup?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.taxGroup?.name ?? '', [Validators.required]],
    description: [this.data.taxGroup?.description ?? ''],
    componentIds: [this.data.taxGroup?.components.map((c) => c.id) ?? ([] as number[])],
    active: [this.data.taxGroup?.active ?? true],
    inclusive: [this.data.taxGroup?.inclusive ?? false]
  });

  constructor() {
    this.organizationService.listAll().subscribe((orgs) => this.organizations.set(orgs));
    this.taxComponentService.listAll().subscribe((components) => {
      this.allTaxComponents.set(components);
      this.refreshComponentOptions(this.form.controls.organizationId.value);
    });

    this.form.controls.organizationId.valueChanges.subscribe((orgId) => {
      this.refreshComponentOptions(orgId);
      this.form.controls.componentIds.setValue([]);
    });
  }

  private refreshComponentOptions(orgId: number | null): void {
    const options = orgId ? this.allTaxComponents().filter((c) => c.organizationId === orgId) : [];
    this.taxComponentOptions.set(options);
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.taxGroupService.update(this.data.taxGroup!.id, {
          name: raw.name,
          description: raw.description,
          componentIds: raw.componentIds,
          active: raw.active,
          inclusive: raw.inclusive
        })
      : this.taxGroupService.create({
          organizationId: raw.organizationId as number,
          code: raw.code,
          name: raw.name,
          description: raw.description,
          componentIds: raw.componentIds
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Tax group ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
