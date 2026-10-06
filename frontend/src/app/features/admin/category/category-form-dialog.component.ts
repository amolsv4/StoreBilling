import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoryService } from '../../../core/services/category.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { Category } from '../../../core/models/category.model';
import { Organization } from '../../../core/models/organization.model';

export interface CategoryFormDialogData {
  mode: 'create' | 'edit';
  category?: Category;
}

@Component({
  selector: 'app-category-form-dialog',
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
  templateUrl: './category-form-dialog.component.html'
})
export class CategoryFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);
  private readonly organizationService = inject(OrganizationService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<CategoryFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as CategoryFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly organizations = signal<Organization[]>([]);
  private readonly allCategories = signal<Category[]>([]);
  readonly parentCategoryOptions = signal<Category[]>([]);

  readonly form = this.fb.nonNullable.group({
    organizationId: [
      { value: this.data.category?.organizationId ?? null, disabled: this.isEdit },
      [Validators.required]
    ],
    code: [{ value: this.data.category?.code ?? '', disabled: this.isEdit }, [Validators.required]],
    name: [this.data.category?.name ?? '', [Validators.required]],
    description: [this.data.category?.description ?? ''],
    parentCategoryId: [this.data.category?.parentCategoryId ?? null],
    active: [this.data.category?.active ?? true]
  });

  constructor() {
    this.organizationService.listAll().subscribe((orgs) => this.organizations.set(orgs));
    this.categoryService.listAll().subscribe((categories) => {
      this.allCategories.set(categories);
      this.refreshParentOptions(this.form.controls.organizationId.value);
    });

    this.form.controls.organizationId.valueChanges.subscribe((orgId) => {
      this.refreshParentOptions(orgId);
      this.form.controls.parentCategoryId.setValue(null);
    });
  }

  /**
   * Filters the parent-category picker to the selected organization and, when editing, excludes the
   * record's own id (the trivial self-parent case). The backend remains the source of truth for
   * deeper ancestor-cycle rejection; its error message surfaces via the global error interceptor.
   */
  private refreshParentOptions(orgId: number | null): void {
    const selfId = this.data.category?.id;
    const options = orgId ? this.allCategories().filter((c) => c.organizationId === orgId && c.id !== selfId) : [];
    this.parentCategoryOptions.set(options);
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    const request$ = this.isEdit
      ? this.categoryService.update(this.data.category!.id, {
          name: raw.name,
          description: raw.description,
          parentCategoryId: raw.parentCategoryId,
          active: raw.active
        })
      : this.categoryService.create({
          organizationId: raw.organizationId as number,
          code: raw.code,
          name: raw.name,
          description: raw.description,
          parentCategoryId: raw.parentCategoryId
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`Category ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', {
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
