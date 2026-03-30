import { useEffect, useState } from "react";

import type { SelectOption } from "../SelectField";
import { TextField } from "../TextField";

export interface CheckboxMultiSelectProps {
  label: string;
  values: readonly string[];
  options: readonly SelectOption[];
  onChange: (values: string[]) => void;
  disabled?: boolean;
  emptyMessage?: string;
  emptySelectionText?: string;
  selectionSummaryText?: (selectedCount: number) => string;
}

function toggleValue(values: readonly string[], nextValue: string): string[] {
  if (values.includes(nextValue)) {
    return values.filter((value) => value !== nextValue);
  }

  return [...values, nextValue];
}

export function CheckboxMultiSelect({
  label,
  values,
  options,
  onChange,
  disabled = false,
  emptyMessage = "Nenhuma opcao disponivel.",
  emptySelectionText = "Categorias",
  selectionSummaryText,
}: CheckboxMultiSelectProps) {
  const [isExpanded, setIsExpanded] = useState(() => values.length > 0);
  const [searchTerm, setSearchTerm] = useState("");
  const contentId = `checkbox-multi-select-${label
    .trim()
    .toLowerCase()
    .replace(/\s+/g, "-")}`;
  const actionButtonClassName =
    "inline-flex min-h-10 items-center justify-center rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-3 py-2 text-xs font-semibold uppercase tracking-[0.2em] text-[color:var(--text-secondary-color)] transition-[transform,background-color,border-color,color,box-shadow] hover:-translate-y-0.5 hover:border-[color:var(--text-color)] hover:text-[color:var(--text-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)]";
  const iconButtonClassName =
    "inline-flex h-10 w-10 items-center justify-center rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] text-[color:var(--text-secondary-color)] transition-[transform,background-color,border-color,color,box-shadow] hover:-translate-y-0.5 hover:border-[color:var(--text-color)] hover:text-[color:var(--text-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)]";

  useEffect(() => {
    if (values.length > 0) {
      setIsExpanded(true);
    }
  }, [values.length]);

  const normalizedSearchTerm = searchTerm.trim().toLowerCase();
  const visibleOptions =
    normalizedSearchTerm.length === 0
      ? options
      : options.filter((option) =>
          option.label.trim().toLowerCase().includes(normalizedSearchTerm),
        );

  return (
    <div className="space-y-2">
      <span className="block text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
        {label}
      </span>

      <div className="rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)]">
        <div className="flex items-center justify-between gap-3 px-4 py-3">
          <div className="space-y-1">
            <p className="font-semibold text-[color:var(--text-color)]">
              {values.length > 0
                ? selectionSummaryText?.(values.length) ??
                  `${values.length} categoria${values.length > 1 ? "s" : ""} selecionada${values.length > 1 ? "s" : ""}`
                : emptySelectionText}
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setIsExpanded((currentValue) => !currentValue)}
              disabled={disabled}
              className={actionButtonClassName}
              aria-expanded={isExpanded}
              aria-controls={contentId}
            >
              {isExpanded ? "Ocultar" : "Mostrar"}
            </button>

            <button
              type="button"
              onClick={() => onChange([])}
              disabled={disabled || values.length === 0}
              className={iconButtonClassName}
              aria-label="Limpar categorias selecionadas"
              title="Limpar categorias selecionadas"
            >
              <svg
                aria-hidden="true"
                viewBox="0 0 16 16"
                className="h-4 w-4"
                fill="none"
              >
                <path
                  d="M4 4 12 12"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                />
                <path
                  d="M12 4 4 12"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                />
              </svg>
            </button>
          </div>
        </div>

        {isExpanded ? (
          <div
            id={contentId}
            className="border-t border-[color:var(--border-color)] p-3"
          >
            {options.length === 0 ? (
              <p className="text-sm text-[color:var(--text-secondary-color)]">{emptyMessage}</p>
            ) : (
              <div>
                <TextField
                  label="Buscar categoria"
                  value={searchTerm}
                  onChange={setSearchTerm}
                  placeholder="Digite o nome da categoria"
                  disabled={disabled}
                />

                {visibleOptions.length === 0 ? (
                  <p className="text-sm text-[color:var(--text-secondary-color)]">
                    Nenhuma categoria encontrada.
                  </p>
                ) : null}

                <div className="mt-3 flex flex-wrap gap-2">
                  {visibleOptions.map((option) => {
                    const isChecked = values.includes(option.value);
                    const optionContainerClassName = isChecked
                      ? "inline-flex min-h-11 items-center gap-3 rounded-2xl border border-[color:var(--text-color)] bg-[color:var(--bg-accent-color)] px-4 py-3 text-sm font-semibold text-[color:var(--text-color)] shadow-[0_0_0_1px_var(--text-color)] transition-[transform,background-color,border-color,color,box-shadow] hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60"
                      : "inline-flex min-h-11 items-center gap-3 rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-4 py-3 text-sm text-[color:var(--text-secondary-color)] transition-[transform,background-color,border-color,color,box-shadow] hover:-translate-y-0.5 hover:border-[color:var(--text-color)] hover:text-[color:var(--text-color)] disabled:cursor-not-allowed disabled:opacity-60";
                    const checkmarkContainerClassName = isChecked
                      ? "flex h-5 w-5 items-center justify-center rounded-md border border-[color:var(--text-color)] bg-[color:var(--text-color)] text-[color:var(--surface-color)] transition-[background-color,border-color,color]"
                      : "flex h-5 w-5 items-center justify-center rounded-md border border-[color:var(--border-color)] bg-[color:var(--surface-color)] text-transparent transition-[background-color,border-color,color]";

                    return (
                      <label
                        key={`${option.value}:${option.label}`}
                        className="cursor-pointer"
                      >
                        <input
                          type="checkbox"
                          value={option.value}
                          checked={isChecked}
                          onChange={() => onChange(toggleValue(values, option.value))}
                          disabled={disabled}
                          className="peer sr-only"
                        />

                        <span className={optionContainerClassName}>
                          <span className={checkmarkContainerClassName}>
                            {isChecked ? (
                              <svg
                                aria-hidden="true"
                                viewBox="0 0 16 16"
                                className="h-3.5 w-3.5"
                                fill="none"
                              >
                                <path
                                  d="M3.5 8.5 6.5 11.5 12.5 4.5"
                                  stroke="currentColor"
                                  strokeWidth="2"
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                />
                              </svg>
                            ) : null}
                          </span>

                          <span>{option.label}</span>
                        </span>
                      </label>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        ) : null}
      </div>
    </div>
  );
}
