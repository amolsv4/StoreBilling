import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../../../core/services/user.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { RoleService } from '../../../core/services/role.service';
import { StoreService } from '../../../core/services/store.service';
import { AppUser } from '../../../core/models/user.model';
import { Organization } from '../../../core/models/organization.model';
import { Role } from '../../../core/models/role.model';
import { Store } from '../../../core/models/store.model';

export interface UserFormDialogData {
  mode: 'create' | 'edit';
  user?: AppUser;
}

/**
 * Create sends the initial password (backend `CreateUserRequest.password`, min 8 chars); edit cannot
 * change username/email/password (`UpdateUserRequest` has none of them), so those controls are
 * disabled in edit mode and only shown for reference.
 */
@Component({
  selector: 'app-user-form-dialog',
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
  templateUrl: './user-form-dialog.component.html'
})
export class UserFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly organizationService = inject(OrganizationService);
  private readonly roleService = inject(RoleService);
  private readonly storeService = inject(StoreService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<UserFormDialogComponent, boolean>);
  // See note in organization-form-dialog.component.ts on why this is a field, not a ctor param property.
  readonly data = inject(MAT_DIALOG_DATA) as UserFormDialogData;

  readonly submitting = signal(false);
  readonly isEdit = this.data.mode === 'edit';
  readonly organizations = signal<Organization[]>([]);
  readonly roles = signal<Role[]>([]);
  private readonly allStores = signal<Store[]>([]);
  readonly storesForSelectedOrg = signal<Store[]>([]);

  readonly form = this.fb.nonNullable.group({
    username: [{ value: this.data.user?.username ?? '', disabled: this.isEdit }, [Validators.required, Validators.minLength(3)]],
    email: [{ value: this.data.user?.email ?? '', disabled: this.isEdit }, [Validators.required, Validators.email]],
    password: [{ value: '', disabled: this.isEdit }, [Validators.required, Validators.minLength(8)]],
    firstName: [this.data.user?.firstName ?? '', [Validators.required]],
    lastName: [this.data.user?.lastName ?? '', [Validators.required]],
    phone: [this.data.user?.phone ?? ''],
    organizationId: [this.data.user?.organizationId ?? (null as number | null)],
    roleCodes: [this.data.user?.roleCodes ?? ([] as string[])],
    allStoreAccess: [this.data.user?.allStoreAccess ?? false],
    storeIds: [this.data.user?.storeIds ?? ([] as number[])],
    active: [this.data.user?.active ?? true],
    accountLocked: [this.data.user?.accountLocked ?? false]
  });

  constructor() {
    this.organizationService.listAll().subscribe((orgs) => this.organizations.set(orgs));
    this.roleService.listAll().subscribe((roles) => this.roles.set(roles));
    this.storeService.listAll().subscribe((stores) => {
      this.allStores.set(stores);
      this.refreshStoresForOrg(this.form.controls.organizationId.value);
    });

    this.form.controls.organizationId.valueChanges.subscribe((orgId) => {
      this.refreshStoresForOrg(orgId);
      this.form.controls.storeIds.setValue([]);
    });

    this.form.controls.allStoreAccess.valueChanges.subscribe((allStores) => {
      if (allStores) {
        this.form.controls.storeIds.disable();
      } else {
        this.form.controls.storeIds.enable();
      }
    });

    if (this.form.controls.allStoreAccess.value) {
      this.form.controls.storeIds.disable();
    }
  }

  private refreshStoresForOrg(orgId: number | null): void {
    this.storesForSelectedOrg.set(orgId ? this.allStores().filter((s) => s.organizationId === orgId) : []);
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();
    const storeIds = raw.allStoreAccess ? [] : raw.storeIds;

    const request$ = this.isEdit
      ? this.userService.update(this.data.user!.id, {
          firstName: raw.firstName,
          lastName: raw.lastName,
          phone: raw.phone,
          organizationId: raw.organizationId,
          allStoreAccess: raw.allStoreAccess,
          active: raw.active,
          accountLocked: raw.accountLocked,
          storeIds,
          roleCodes: raw.roleCodes
        })
      : this.userService.create({
          username: raw.username,
          email: raw.email,
          password: raw.password,
          firstName: raw.firstName,
          lastName: raw.lastName,
          phone: raw.phone,
          organizationId: raw.organizationId,
          allStoreAccess: raw.allStoreAccess,
          storeIds,
          roleCodes: raw.roleCodes
        });

    request$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.snackBar.open(`User ${this.isEdit ? 'updated' : 'created'} successfully.`, 'Dismiss', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => this.submitting.set(false)
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
