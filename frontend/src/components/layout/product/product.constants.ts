import type { ProductSortParam } from "../../../api";
import type { SelectOption } from "../../common/SelectField";

export const DEFAULT_PAGE_SIZE = 8;
export const DEFAULT_SORT: ProductSortParam = "name,asc";
export const PRODUCT_PAGINATION_SIBLING_COUNT = 2;
export const PRODUCT_SECONDARY_ACTION_BUTTON_CLASSES =
  "inline-flex min-h-11 w-full items-center justify-center rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-4 py-2 text-sm font-semibold text-[color:var(--text-secondary-color)] transition-[transform,background-color,border-color,color,box-shadow] hover:-translate-y-0.5 hover:border-[color:var(--text-color)] hover:text-[color:var(--text-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)] lg:w-auto";

export const PRODUCT_SORT_OPTIONS = [
  { label: "Nome (A-Z)", value: "name,asc" },
  { label: "Nome (Z-A)", value: "name,desc" },
  { label: "Preco (menor primeiro)", value: "price,asc" },
  { label: "Preco (maior primeiro)", value: "price,desc" },
  { label: "Custo (menor primeiro)", value: "cost,asc" },
  { label: "Custo (maior primeiro)", value: "cost,desc" },
  { label: "Margem de lucro (menor primeiro)", value: "profitMargin,asc" },
  { label: "Margem de lucro (maior primeiro)", value: "profitMargin,desc" },
  { label: "Categoria (A-Z)", value: "category,asc" },
  { label: "Codigo (A-Z)", value: "code,asc" },
] satisfies readonly (SelectOption & { value: ProductSortParam })[];

export const PRODUCT_PAGE_SIZE_OPTIONS = [
  { label: "8 por pagina", value: "8" },
  { label: "12 por pagina", value: "12" },
  { label: "24 por pagina", value: "24" },
  { label: "48 por pagina", value: "48" },
] satisfies readonly SelectOption[];
