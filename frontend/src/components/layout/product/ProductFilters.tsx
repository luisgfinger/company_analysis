import { useState } from "react";

import { CheckboxMultiSelect } from "../../common/CheckboxMultiSelect";
import { SearchBar } from "../../common/SearchBar";
import { SelectField, type SelectOption } from "../../common/SelectField";
import { TextField } from "../../common/TextField";
import {
  DEFAULT_PAGE_SIZE,
  DEFAULT_SORT,
  PRODUCT_PAGE_SIZE_OPTIONS,
  PRODUCT_SECONDARY_ACTION_BUTTON_CLASSES,
  PRODUCT_SORT_OPTIONS,
} from "./product.constants";

export interface ProductFilterFormValues {
  q: string;
  code: string;
  barcode: string;
  category: string[];
  excludedCategories: string[];
  sort: string;
  size: string;
}

export interface ProductFiltersProps {
  values: ProductFilterFormValues;
  categoryOptions: readonly SelectOption[];
  isCategoryLoading?: boolean;
  barcodeDisabled?: boolean;
  disabled?: boolean;
  onValueChange: (
    field: keyof ProductFilterFormValues,
    value: string | string[],
  ) => void;
  onReset: () => void;
}

function hasActiveFilters(values: ProductFilterFormValues): boolean {
  return (
    values.q.trim().length > 0 ||
    values.code.trim().length > 0 ||
    values.barcode.trim().length > 0 ||
    values.category.length > 0 ||
    values.excludedCategories.length > 0 ||
    values.sort !== DEFAULT_SORT ||
    values.size !== String(DEFAULT_PAGE_SIZE)
  );
}

function hasActiveAdvancedFilters(values: ProductFilterFormValues): boolean {
  return (
    values.code.trim().length > 0 ||
    values.barcode.trim().length > 0 ||
    values.category.length > 0 ||
    values.excludedCategories.length > 0 ||
    values.sort !== DEFAULT_SORT ||
    values.size !== String(DEFAULT_PAGE_SIZE)
  );
}

export function ProductFilters({
  values,
  categoryOptions,
  isCategoryLoading = false,
  barcodeDisabled = false,
  disabled = false,
  onValueChange,
  onReset,
}: ProductFiltersProps) {
  const [isExpanded, setIsExpanded] = useState(() =>
    hasActiveAdvancedFilters(values),
  );

  return (
    <div className="mt-6 space-y-4 rounded-[1.75rem] border border-[color:var(--border-color)] bg-[color:var(--bg-color)] p-4 shadow-[0_12px_36px_var(--bg-accent-color)] sm:p-5">
      <SearchBar
        label="Busca geral"
        value={values.q}
        onChange={(value) => onValueChange("q", value)}
        placeholder="Busque por nome, codigo, categoria ou codigo de barras"
        disabled={disabled}
        onClear={() => onValueChange("q", "")}
      />

      <div className="rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)]">
        <button
          type="button"
          onClick={() => setIsExpanded((currentValue) => !currentValue)}
          className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left text-sm text-[color:var(--text-secondary-color)] transition-[color,transform] hover:text-[color:var(--text-color)]"
          aria-expanded={isExpanded}
          aria-controls="product-advanced-filters"
        >
          <div className="space-y-1">
            <p className="font-semibold text-[color:var(--text-color)]">
              Filtros
            </p>
          </div>

          <div className="flex items-center gap-2">
            {hasActiveAdvancedFilters(values) ? (
              <span className="rounded-full bg-[color:var(--bg-accent-color)] px-2.5 py-1 text-[11px] font-semibold uppercase tracking-[0.18em] text-[color:var(--text-color)]">
                Ativos
              </span>
            ) : null}

            <span className="text-xs font-semibold uppercase tracking-[0.2em] text-[color:var(--text-secondary-color)]">
              {isExpanded ? "Ocultar" : "Mostrar"}
            </span>
          </div>
        </button>

        {isExpanded ? (
          <div
            id="product-advanced-filters"
            className="grid gap-4 border-t border-[color:var(--border-color)] px-4 py-4 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)]"
          >
            <TextField
              label="Codigo"
              value={values.code}
              onChange={(value) => onValueChange("code", value)}
              placeholder="Ex.: SKU-001"
              disabled={disabled}
            />

            <TextField
              label="Codigo de barras"
              value={values.barcode}
              onChange={(value) => onValueChange("barcode", value)}
              placeholder={
                barcodeDisabled ? "Consulta rapida ativa" : "Ex.: 7890000000000"
              }
              disabled={disabled || barcodeDisabled}
            />

            <CheckboxMultiSelect
              label="Categoria"
              values={values.category}
              options={categoryOptions}
              onChange={(value) => onValueChange("category", value)}
              disabled={disabled || isCategoryLoading}
              emptySelectionText="Categorias"
              emptyMessage={
                isCategoryLoading
                  ? "Carregando categorias..."
                  : "Nenhuma categoria disponivel."
              }
            />

            <SelectField
              label="Ordenar por"
              value={values.sort}
              options={PRODUCT_SORT_OPTIONS}
              onChange={(value) => onValueChange("sort", value)}
              disabled={disabled}
            />

            <SelectField
              label="Itens por pagina"
              value={values.size}
              options={PRODUCT_PAGE_SIZE_OPTIONS}
              onChange={(value) => onValueChange("size", value)}
              disabled={disabled}
            />

            <CheckboxMultiSelect
              label="Excluir categorias"
              values={values.excludedCategories}
              options={categoryOptions}
              onChange={(value) => onValueChange("excludedCategories", value)}
              disabled={disabled || isCategoryLoading}
              emptySelectionText="Categorias excluidas"
              selectionSummaryText={(selectedCount) =>
                `${selectedCount} categoria${selectedCount > 1 ? "s" : ""} excluida${selectedCount > 1 ? "s" : ""}`
              }
              emptyMessage={
                isCategoryLoading
                  ? "Carregando categorias..."
                  : "Nenhuma categoria disponivel."
              }
            />
            <div>
              <></>
            </div>
            <div className="flex items-end justify-start lg:justify-center">
              <button
                type="button"
                onClick={() => {
                  onReset();
                  setIsExpanded(false);
                }}
                disabled={disabled || !hasActiveFilters(values)}
                className={PRODUCT_SECONDARY_ACTION_BUTTON_CLASSES}
              >
                Limpar filtros
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
