import { useState } from "react";
import styles from "./GameHandle.module.css";

type GameHandleProps = {
  handle: string;
  isPasswordProtected: boolean;
  copyLabel: string;
  copiedLabel: string;
  passwordProtectedLabel: string;
};

export function GameHandle({
  handle,
  isPasswordProtected,
  copyLabel,
  copiedLabel,
  passwordProtectedLabel,
}: GameHandleProps) {
  const [isCopied, setIsCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(handle).then(() => {
      setIsCopied(true);
      setTimeout(() => setIsCopied(false), 2000);
    });
  };

  return (
    <div className={styles.container}>
      <code className={styles.handle}>{handle}</code>
      {isPasswordProtected && (
        <span className={styles.lock} aria-hidden="false">
          <span className="sr-only">{passwordProtectedLabel}</span>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="1em"
            height="1em"
            aria-hidden="true"
            focusable="false"
          >
            <path d="M12 1C9.24 1 7 3.24 7 6v2H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2h-2V6c0-2.76-2.24-5-5-5zm0 2c1.66 0 3 1.34 3 3v2H9V6c0-1.66 1.34-3 3-3zm0 9c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2z" />
          </svg>
        </span>
      )}
      <button
        type="button"
        className={`${styles.copyButton} ${isCopied ? styles.copied : ""}`}
        onClick={handleCopy}
        aria-label={isCopied ? copiedLabel : copyLabel}
      >
        {isCopied ? (
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="1em"
            height="1em"
            aria-hidden="true"
            focusable="false"
          >
            <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z" />
          </svg>
        ) : (
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="1em"
            height="1em"
            aria-hidden="true"
            focusable="false"
          >
            <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z" />
          </svg>
        )}
      </button>
    </div>
  );
}
