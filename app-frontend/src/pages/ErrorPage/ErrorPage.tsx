import { Link, useParams } from "react-router";
import { useTranslation } from "../../i18n";
import styles from "./ErrorPage.module.css";

type ErrorType = "not-found" | "already-started" | "kicked" | "invalid-magic-link";

type ErrorPageProps = {
  errorType?: ErrorType;
};

const ERROR_KEYS: Record<ErrorType, string> = {
  "not-found": "game.error.notFound",
  "already-started": "game.error.alreadyStarted",
  kicked: "game.error.kicked",
  "invalid-magic-link": "game.error.invalidMagicLink",
};

export function ErrorPage({ errorType: propErrorType }: ErrorPageProps) {
  const { t } = useTranslation();
  const { errorType: paramErrorType } = useParams<{ errorType: string }>();

  const rawErrorType = propErrorType ?? paramErrorType;
  const resolvedErrorType: ErrorType =
    rawErrorType === "not-found" ||
    rawErrorType === "already-started" ||
    rawErrorType === "kicked" ||
    rawErrorType === "invalid-magic-link"
      ? rawErrorType
      : "not-found";

  const messageKey = ERROR_KEYS[resolvedErrorType];

  return (
    <main className={styles.page}>
      <h1 className={styles.title}>{t("game.error.title")}</h1>
      <p className={styles.message}>{t(messageKey)}</p>
      <Link to="/" className={styles.homeLink}>
        {t("game.error.home")}
      </Link>
    </main>
  );
}
