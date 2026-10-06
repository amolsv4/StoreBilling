import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryService } from '../../../core/services/category.service';
import { Category } from '../../../core/models/category.model';
import { DataTableColumn, DataTableComponent } from '../../../shared/components/data-table/data-table.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { CategoryFormDialogComponent, CategoryFormDialogData } from './category-form-dialog.component';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, DataTableComponent, PageHeaderComponent],
  templateUrl: './category-list.component.html'
})
export class CategoryListComponent {
  private readonly categoryService = inject(CategoryService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly categories = signal<Category[]>([]);
  readonly totalElements = signal(0);
  pageIndex = 0;
  pageSize = 10;

  readonly columns: DataTableColumn<Category>[] = [
    { key: 'code', header: 'Code' },
    { key: 'name', header: 'Name' },
    { key: 'organizationName', header: 'Organization' },
    { key: 'parentCategoryName', header: 'Parent Category' }
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.categoryService.list(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.categories.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openCreate(): void {
    this.openDialog({ mode: 'create' });
  }

  openEdit(category: Category): void {
    this.openDialog({ mode: 'edit', category });
  }

  private openDialog(data: CategoryFormDialogData): void {
    this.dialog
      .open(CategoryFormDialogComponent, { width: '700px', data, disableClose: true })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  onToggleActive(category: Category): void {
    if (!category.active) {
      this.setActive(category, true);
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Deactivate category',
        message: `Deactivate "${category.name}"? It can be reactivated later.`,
        confirmText: 'Deactivate'
      }
    });
    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.setActive(category, false);
      }
    });
  }

  private setActive(category: Category, active: boolean): void {
    const { id, organizationCode, organizationName, parentCategoryCode, parentCategoryName, ...rest } = category;
    this.categoryService.update(id, { ...rest, active }).subscribe({
      next: () => {
        this.snackBar.open(`Category ${active ? 'reactivated' : 'deactivated'}.`, 'Dismiss', { duration: 3000 });
        this.load();
      }
    });
  }
}
