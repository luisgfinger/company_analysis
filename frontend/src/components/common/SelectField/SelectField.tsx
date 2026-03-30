export interface SelectOption {
  label: string;
  value: string;
}

interface BaseSelectFieldProps {
  label: string;
  options: readonly SelectOption[];
  disabled?: boolean;
}

interface SingleSelectFieldProps extends BaseSelectFieldProps {
  multiple?: false;
  value: string;
  onChange: (value: string) => void;
}

interface MultipleSelectFieldProps extends BaseSelectFieldProps {
  multiple: true;
  value: string[];
  onChange: (value: string[]) => void;
  size?: number;
}

export type SelectFieldProps = SingleSelectFieldProps | MultipleSelectFieldProps;

export function SelectField(props: SelectFieldProps) {
  const { label, options, disabled = false } = props;
  const defaultSize = Math.min(Math.max(options.length, 4), 8);

  return (
    <label className="space-y-2">
      <span className="block text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
        {label}
      </span>

      <select
        value={props.value}
        onChange={(event) => {
          if (props.multiple) {
            props.onChange(
              Array.from(event.target.selectedOptions, (option) => option.value),
            );

            return;
          }

          props.onChange(event.target.value);
        }}
        disabled={disabled}
        multiple={props.multiple}
        size={props.multiple ? props.size ?? defaultSize : undefined}
        className="min-h-12 w-full rounded-2xl border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-4 py-3 text-sm text-[color:var(--text-color)] outline-none transition-[background-color,border-color,color,box-shadow] focus:border-[color:var(--text-color)] focus:ring-4 focus:ring-[color:var(--bg-accent-color)] disabled:cursor-not-allowed disabled:text-[color:var(--text-secondary-color)]"
      >
        {options.map((option) => (
          <option key={`${option.value}:${option.label}`} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}
