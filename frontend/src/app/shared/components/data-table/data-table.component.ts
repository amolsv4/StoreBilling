import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSlideToggleChange, MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

export interface DataTableColumn<T> {
  /** Property key on the row object, used both to read the value and as the mat-table column id. */
  key: string;
  header: string;
  /** Optional custom cell formatter; defaults to reading `row[key]` as a string. */
  cell?: (row: T) => string;
  /** Renders this column as a boolean "Yes/No" chip instead of plain text. */
  isBoolean?: boolean;
}

/**
 * Generic MatTable + paginator wrapper. Pagination is server-side: the parent owns page/size state
 * and reloads data on the `page` output, this component just renders whatever `data` it is given.
 */
@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatTooltipModule
  ],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss'
})
export class DataTableComponent<T> {
  @Input() columns: DataTableColumn<T>[] = [];
  @Input() data: T[] = [];
  @Input() totalElements = 0;
  @Input() pageSize = 10;
  @Input() pageIndex = 0;
  @Input() pageSizeOptions: number[] = [10, 25, 50];
  @Input() loading = false;
  @Input() showEdit = true;
  @Input() showActiveToggle = true;
  @Input() editDisabled = false;
  @Input() toggleDisabled = false;
  @Input() emptyMessage = 'No records found.';
  /** Additive, default-preserving (Phase 3): lets a caller relabel the action button/icon - e.g.
   *  Held Bills uses editIcon="play_circle"/editLabel="Resume" instead of the default edit pencil.
   *  Every existing screen that doesn't set these keeps the original "edit"/"Edit" look. */
  @Input() editIcon = 'edit';
  @Input() editLabel = 'Edit';

  @Output() page = new EventEmitter<PageEvent>();
  @Output() edit = new EventEmitter<T>();
  @Output() toggleActive = new EventEmitter<T>();

  get displayedColumns(): string[] {
    const keys = this.columns.map((c) => c.key);
    if (this.showActiveToggle) {
      keys.push('active');
    }
    if (this.showEdit) {
      keys.push('actions');
    }
    return keys;
  }

  cellValue(column: DataTableColumn<T>, row: T): string {
    if (column.cell) {
      return column.cell(row);
    }
    const value = this.rawValue(column, row);
    return value == null ? '' : String(value);
  }

  boolValue(column: DataTableColumn<T>, row: T): boolean {
    return !!this.rawValue(column, row);
  }

  /**
   * Reads `row.active` dynamically instead of via a generic constraint (Phase 3, additive): a
   * `T extends { active?: boolean }` constraint made template-side generic type inference collapse
   * to the constraint's own (all-optional) shape for any row type that genuinely lacks `active`
   * (Sales/Inventory responses are the first ones in this app that do) - every `[columns]`/`[data]`
   * binding on those screens then failed to type-check against the wrongly-inferred `T`. Dropping the
   * constraint and reading `active` the same dynamic way `cellValue`/`boolValue` already read
   * arbitrary column keys fixes inference for every caller and is runtime-identical for the 11
   * existing screens, which only ever render this column when `showActiveToggle` is true (the
   * default) and their row types really do have a boolean `active`.
   */
  isRowActive(row: T): boolean {
    return !!(row as unknown as Record<string, unknown>)['active'];
  }

  /** Centralizes the one necessary `any`-ish cast for reading a dynamic column key off a typed row. */
  private rawValue(column: DataTableColumn<T>, row: T): unknown {
    return (row as unknown as Record<string, unknown>)[column.key];
  }

  onPage(event: PageEvent): void {
    this.page.emit(event);
  }

  onEdit(row: T): void {
    this.edit.emit(row);
  }

  onToggleActive(row: T, event: MatSlideToggleChange): void {
    // Revert the visual toggle immediately; the parent is responsible for calling the update API and
    // re-rendering `data` once it succeeds (so the toggle reflects the persisted state, not an
    // optimistic guess).
    event.source.checked = this.isRowActive(row);
    this.toggleActive.emit(row);
  }
}
