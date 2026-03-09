import { createContext, useCallback, useEffect, useState } from "react";

type I18nContextType = {
  t: (key: string, params?: Record<string, string>) => string;
};

export const I18nContext = createContext<I18nContextType | null>(null);

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [translations, setTranslations] = useState<Record<string, string>>({});

  useEffect(() => {
    fetch("/api/i18n/fr")
      .then((r) => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json() as Promise<Record<string, string>>;
      })
      .then(setTranslations)
      .catch(() => {
        console.warn("i18n: failed to load translations");
      });
  }, []);

  const t = useCallback(
    (key: string, params?: Record<string, string>) => {
      const value = translations[key] ?? key;
      if (!params) return value;
      return Object.entries(params).reduce(
        (result, [param, replacement]) =>
          result.replace(new RegExp(`\\{${param}\\}`, "g"), replacement),
        value,
      );
    },
    [translations],
  );

  return (
    <I18nContext.Provider value={{ t }}>{children}</I18nContext.Provider>
  );
}
