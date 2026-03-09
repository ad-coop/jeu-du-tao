import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./tokens.css";
import { I18nProvider } from "./i18n";
import { ToastProvider } from "./components/Toast";
import App from "./App";

const rootElement = document.getElementById("root");
if (!rootElement) throw new Error("Root element not found");

createRoot(rootElement).render(
  <StrictMode>
    <I18nProvider>
      <ToastProvider>
        <App />
      </ToastProvider>
    </I18nProvider>
  </StrictMode>,
);
