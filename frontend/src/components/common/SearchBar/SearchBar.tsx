export interface SearchBarProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  onClear?: () => void;
}

export function SearchBar({
  label,
  value,
  onChange,
  placeholder = "Buscar",
  disabled = false,
  onClear,
}: SearchBarProps) {
  return (
    <div className="space-y-2">
      <label className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
        {label}
      </label>

      <div className="flex flex-col gap-3 sm:flex-row">
        <input
          type="search"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder={placeholder}
          disabled={disabled}
          className="min-h-12 flex-1 rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-4 py-3 text-sm text-[color:var(--text-color)] outline-none transition-[background-color,border-color,color,box-shadow] placeholder:text-[color:var(--text-secondary-color)] focus:border-[color:var(--text-color)] focus:ring-4 focus:ring-[color:var(--bg-accent-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)]"
        />

        {onClear ? (
          <button
            type="button"
            onClick={onClear}
            disabled={disabled || value.trim().length === 0}
            className="inline-flex min-h-12 items-center justify-center rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-5 py-3 text-sm font-semibold text-[color:var(--text-secondary-color)] transition-[transform,background-color,border-color,color,box-shadow] hover:-translate-y-0.5 hover:border-[color:var(--text-color)] hover:text-[color:var(--text-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)]"
          >
            Limpar busca
          </button>
        ) : null}
      </div>
    </div>
  );
}
