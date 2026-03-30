type PaginationGap = "left-gap" | "right-gap";
type PaginationItem = number | PaginationGap;

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  disabled?: boolean;
  siblingCount?: number;
}

function createPaginationItems(
  currentPage: number,
  totalPages: number,
  siblingCount: number,
): PaginationItem[] {
  if (totalPages <= 0) {
    return [];
  }

  const safeCurrentPage = Math.min(Math.max(currentPage, 1), totalPages);
  const safeSiblingCount = Math.max(siblingCount, 1);
  const maxVisibleItems = safeSiblingCount * 2 + 5;

  if (totalPages <= maxVisibleItems) {
    return Array.from({ length: totalPages }, (_, index) => index + 1);
  }

  const items: PaginationItem[] = [1];
  const leftBoundary = Math.max(safeCurrentPage - safeSiblingCount, 2);
  const rightBoundary = Math.min(
    safeCurrentPage + safeSiblingCount,
    totalPages - 1,
  );

  if (leftBoundary > 2) {
    items.push("left-gap");
  } else {
    for (let page = 2; page < leftBoundary; page += 1) {
      items.push(page);
    }
  }

  for (let page = leftBoundary; page <= rightBoundary; page += 1) {
    items.push(page);
  }

  if (rightBoundary < totalPages - 1) {
    items.push("right-gap");
  } else {
    for (let page = rightBoundary + 1; page < totalPages; page += 1) {
      items.push(page);
    }
  }

  items.push(totalPages);

  return items;
}

function getButtonClasses(isActive: boolean, isDisabled: boolean): string {
  const baseClasses =
    "inline-flex min-w-11 items-center justify-center rounded-2xl border px-3 py-2 text-sm font-semibold transition-[transform,background-color,border-color,color,box-shadow]";

  if (isActive) {
    return `${baseClasses} border-[color:var(--text-color)] bg-[color:var(--text-color)] text-[color:var(--surface-color)] shadow-[0_12px_30px_var(--bg-accent-color)]`;
  }

  if (isDisabled) {
    return `${baseClasses} cursor-not-allowed border-[color:var(--border-color)] bg-[color:var(--surface-disabled-color)] text-[color:var(--text-secondary-color)]`;
  }

  return `${baseClasses} border-[color:var(--border-color)] bg-[color:var(--surface-color)] text-[color:var(--text-color)] shadow-sm hover:-translate-y-0.5 hover:border-[color:var(--text-color)] hover:text-[color:var(--text-color)]`;
}

export function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  disabled = false,
  siblingCount = 1,
}: PaginationProps) {
  if (totalPages <= 0) {
    return null;
  }

  const safeCurrentPage = Math.min(Math.max(currentPage, 1), totalPages);
  const paginationItems = createPaginationItems(
    safeCurrentPage,
    totalPages,
    siblingCount,
  );

  function handlePageChange(page: number) {
    if (disabled || page === safeCurrentPage || page < 1 || page > totalPages) {
      return;
    }

    onPageChange(page);
  }

  return (
    <nav aria-label="Paginação de resultados" className="w-full">
      <div className="flex flex-wrap items-center justify-center gap-2">
        <button
          type="button"
          className={getButtonClasses(false, disabled || safeCurrentPage === 1)}
          onClick={() => handlePageChange(safeCurrentPage - 1)}
          disabled={disabled || safeCurrentPage === 1}
          aria-label="Ir para a página anterior"
        >
          Anterior
        </button>

        {paginationItems.map((item) => {
          if (typeof item !== "number") {
            return (
              <span
                key={item}
                className="inline-flex min-w-11 items-center justify-center px-1 text-sm font-semibold text-[color:var(--text-secondary-color)]"
                aria-hidden="true"
              >
                ...
              </span>
            );
          }

          const isActive = item === safeCurrentPage;

          return (
            <button
              key={item}
              type="button"
              className={getButtonClasses(isActive, disabled)}
              onClick={() => handlePageChange(item)}
              disabled={disabled}
              aria-current={isActive ? "page" : undefined}
              aria-label={`Ir para a página ${item}`}
            >
              {item}
            </button>
          );
        })}

        <button
          type="button"
          className={getButtonClasses(
            false,
            disabled || safeCurrentPage === totalPages,
          )}
          onClick={() => handlePageChange(safeCurrentPage + 1)}
          disabled={disabled || safeCurrentPage === totalPages}
          aria-label="Ir para a próxima página"
        >
          Próxima
        </button>
      </div>
    </nav>
  );
}
