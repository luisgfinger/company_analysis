type Theme = "light" | "dark";

interface NavbarProps {
  theme: Theme;
  onToggleTheme: () => void;
}

export function Navbar({ theme, onToggleTheme }: NavbarProps) {
  const nextThemeLabel = theme === "light" ? "Escuro" : "Claro";

  return (
    <header className="px-4 pt-4 sm:px-6 lg:px-8">
      <div className="mx-auto flex w-full max-w-6xl items-center justify-between gap-4 rounded-full border border-[color:var(--border-color)] bg-[color:var(--surface-color)] px-4 py-3 shadow-[0_18px_45px_var(--bg-accent-color)] backdrop-blur sm:px-6">
        <span className="text-sm font-semibold uppercase tracking-[0.28em] text-[color:var(--text-color)] sm:text-base">
          Company Analysis
        </span>

        <button
          type="button"
          onClick={onToggleTheme}
          aria-label={`Ativar tema ${nextThemeLabel.toLowerCase()}`}
          className="inline-flex items-center justify-center rounded-full border border-[color:var(--border-color)] px-4 py-2 text-sm font-medium text-[color:var(--text-color)] transition-[transform,background-color,border-color,color,box-shadow] duration-200 hover:-translate-y-0.5 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[color:var(--text-color)]"
        >
          Tema {nextThemeLabel}
        </button>
      </div>
    </header>
  );
}
