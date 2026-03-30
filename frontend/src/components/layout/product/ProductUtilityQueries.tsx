import {
  PRODUCT_UTILITY_QUERIES,
  type ProductUtilityQueryId,
} from "./product.utility-queries";

export interface ProductUtilityQueriesProps {
  selectedQueryId: ProductUtilityQueryId | null;
  disabled?: boolean;
  onSelect: (queryId: ProductUtilityQueryId | null) => void;
}

function getButtonClasses(isActive: boolean, disabled: boolean): string {
  const baseClasses =
    "flex min-h-8 max-w-58 flex-col items-center rounded-[1.5rem] border px-2 py-2 transition-[transform,background-color,border-color,color,box-shadow]";

  if (isActive) {
    return `${baseClasses} border-[color:var(--text-color)] bg-[color:var(--surface-color)] shadow-[0_12px_30px_var(--bg-accent-color)]`;
  }

  if (disabled) {
    return `${baseClasses} cursor-not-allowed border-[color:var(--border-color)] bg-[color:var(--surface-color)] opacity-70`;
  }

  return `${baseClasses} border-[color:var(--border-color)] bg-[color:var(--surface-color)] hover:-translate-y-0.5 hover:border-[color:var(--text-color)]`;
}

export function ProductUtilityQueries({
  selectedQueryId,
  disabled = false,
  onSelect,
}: ProductUtilityQueriesProps) {
  return (
    <div className="mt-6 gap-4 md:flex rounded-[1.75rem] border border-[color:var(--border-color)] bg-[color:var(--bg-color)] p-4 shadow-[0_12px_36px_var(--bg-accent-color)] sm:p-5">
      <div className="space-y-1 flex items-center">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Consultas rapidas
        </p>
      </div>

      <div className="grid gap-3 md:grid-cols-2">
        {PRODUCT_UTILITY_QUERIES.map((query) => {
          const isActive = selectedQueryId === query.id;

          return (
            <button
              key={query.id}
              type="button"
              className={getButtonClasses(isActive, disabled)}
              onClick={() => onSelect(isActive ? null : query.id)}
              disabled={disabled}
              aria-pressed={isActive}
            >
              <span className="text-sm font-semibold text-[color:var(--text-color)]">
                {query.title}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
