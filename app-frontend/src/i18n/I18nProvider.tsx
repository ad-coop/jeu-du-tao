import { createContext, useCallback, useEffect, useState } from "react";

type I18nContextType = {
  t: (key: string) => string;
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
    (key: string) => translations[key] ?? key,
    [translations],
  );

  return (
    <I18nContext.Provider value={{ t }}>{children}</I18nContext.Provider>
  );
}
