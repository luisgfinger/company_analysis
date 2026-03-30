import { useEffect, useState } from "react";

import { Navbar } from "./components/layout/Navbar";
import { Home } from "./pages/Home";

type Theme = "light" | "dark";

const THEME_STORAGE_KEY = "company-analysis-theme";

function getInitialTheme(): Theme {
  if (typeof window === "undefined") {
    return "light";
  }

  const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);

  if (storedTheme === "light" || storedTheme === "dark") {
    return storedTheme;
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

function App() {
  const [theme, setTheme] = useState<Theme>(getInitialTheme);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem(THEME_STORAGE_KEY, theme);
  }, [theme]);

  function handleToggleTheme() {
    setTheme((currentTheme) => (currentTheme === "light" ? "dark" : "light"));
  }

  return (
    <>
      <Navbar theme={theme} onToggleTheme={handleToggleTheme} />

      <main>
        <Home />
      </main>
    </>
  );
}

export default App;
