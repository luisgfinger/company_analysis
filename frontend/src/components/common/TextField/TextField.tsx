export interface TextFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

export function TextField({
  label,
  value,
  onChange,
  placeholder,
  disabled = false,
}: TextFieldProps) {
  return (
    <label className="space-y-2">
      <span className="block text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
        {label}
      </span>

      <input
        type="text"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        className="min-h-12 w-full rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-4 py-3 text-sm text-[color:var(--text-color)] outline-none transition-[background-color,border-color,color,box-shadow] placeholder:text-[color:var(--text-secondary-color)] focus:border-[color:var(--text-color)] focus:ring-4 focus:ring-[color:var(--bg-accent-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)]"
      />
    </label>
  );
}
