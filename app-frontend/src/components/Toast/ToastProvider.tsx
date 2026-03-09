import { createContext, useCallback, useState } from "react";
import styles from "./Toast.module.css";

type ToastContextType = {
  showToast: (message: string) => void;
};

export const ToastContext = createContext<ToastContextType | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [message, setMessage] = useState<string | null>(null);

  const showToast = useCallback((msg: string) => {
    setMessage(msg);
    setTimeout(() => setMessage(null), 3000);
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div aria-live="polite" role="status" className={styles.container}>
        {message !== null && (
          <div className={styles.toast}>{message}</div>
        )}
      </div>
    </ToastContext.Provider>
  );
}
