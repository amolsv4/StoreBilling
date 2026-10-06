import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

export interface LineItemsColumn<T> {
  /** Property key on the row object, used both to read the value and as the mat-table column id. */
  key: string;
  header: string;
  /** Read-only display formatter; defaults to reading `row[key]` as a string. Ignored when `editable`. */
  cell?: (row: T) => string;
  /** Renders this column as an inline-editable numeric input bound to `row[key]`. */
  editable?: boolean;
  /** Input step for editable numeric cells - Sales/Inventory quantities are BigDecimal(12,3), so
   *  the default is fine-grained enough for fractional-weight UOMs (KG/GRAM/LITER). */
  step?: number;
  min?: number;
}

export interface LineItemCellChange<T> {
  row: T;
  key: string;
  value: number;
}

/**
 * Purpose-built editable grid (inline-editable numeric cells, remove-row button, column-config API
 * mirroring `DataTableColumn`'s style) reused by the POS cart and the three inventory
 * "header+lines" screens (Purchase Receipt/Stock Transfer/Stock Take). Adding a row is *not* this
 * component's job - the parent screen places a `ProductSearchComponent` above/beside the grid and
 * calls the relevant add-line API itself, then passes the server's updated `rows` back down here.
 * This component never recomputes anything - `cellChange` just reports the raw edited value; the
 * parent is responsible for calling the update API and re-rendering `rows` with the server's
 * recalculated response (mirrors `DataTableComponent`'s toggle-then-re-render convention).
 */
@Component({
  selector: 'app-line-items-editor',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './line-items-editor.component.html',
  styleUrl: './line-items-editor.component.scss'
})
export class LineItemsEditorComponent<T extends { id: number }> {
  @Input() columns: LineItemsColumn<T>[] = [];
  @Input() rows: T[] = [];
  /** Once true (e.g. the header is no longer DRAFT), editable cells render read-only and the
   *  remove button is hidden - matches the posted-is-immutable rule shared by Purchase/Transfer/
   *  StockTake and completed Sales. */
  @Input() disabled = false;
  @Input() showRemove = true;
  @Input() emptyMessage = 'No lines added yet.';

  @Output() cellChange = new EventEmitter<LineItemCellChange<T>>();
  @Output() remove = new EventEmitter<T>();

  get displayedColumns(): string[] {
    const keys = this.columns.map((c) => c.key);
    if (this.showRemove) {
      keys.push('remove');
    }
    return keys;
  }

  cellValue(column: LineItemsColumn<T>, row: T): string {
    if (column.cell) {
      return column.cell(row);
    }
    const value = this.rawValue(column, row);
    return value == null ? '' : String(value);
  }

  numericValue(column: LineItemsColumn<T>, row: T): number {
    const value = this.rawValue(column, row);
    return typeof value === 'number' ? value : 0;
  }

  onCellCommit(column: LineItemsColumn<T>, row: T, event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    if (Number.isNaN(value)) {
      input.value = String(this.numericValue(column, row));
      return;
    }
    this.cellChange.emit({ row, key: column.key, value });
  }

  onRemove(row: T): void {
    this.remove.emit(row);
  }

  /** Centralizes the one necessary `any`-ish cast for reading a dynamic column key off a typed row
   *  (same pattern as `DataTableComponent`). */
  private rawValue(column: LineItemsColumn<T>, row: T): unknown {
    return (row as unknown as Record<string, unknown>)[column.key];
  }
}
