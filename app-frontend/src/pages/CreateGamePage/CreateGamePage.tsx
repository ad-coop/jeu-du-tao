import { useState } from "react";
import { useNavigate } from "react-router";
import { gameApi } from "../../api/gameApi";
import type { ApiError } from "../../api/gameApi";
import { FormField } from "../../components/FormField";
import { useGameSession } from "../../hooks/useGameSession";
import { useTranslation } from "../../i18n";
import styles from "./CreateGamePage.module.css";

function validateUserName(value: string): string | undefined {
  const trimmed = value.trim();
  if (!trimmed) return "game.create.error.userNameRequired";
  if (trimmed.length > 50) return "game.create.error.userNameTooLong";
  return undefined;
}

function validateEmail(value: string): string | undefined {
  if (!value) return undefined;
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(value)) return "game.create.error.emailInvalid";
  return undefined;
}

function validatePassword(value: string): string | undefined {
  if (!value) return undefined;
  if (value.length > 100) return "game.create.error.passwordTooLong";
  return undefined;
}

export function CreateGamePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { setSession } = useGameSession();

  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [userNameError, setUserNameError] = useState<string | undefined>();
  const [emailError, setEmailError] = useState<string | undefined>();
  const [passwordError, setPasswordError] = useState<string | undefined>();
  const [submitError, setSubmitError] = useState<string | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const userNameErr = validateUserName(userName);
    const emailErr = validateEmail(email);
    const passwordErr = validatePassword(password);

    setUserNameError(userNameErr ? t(userNameErr) : undefined);
    setEmailError(emailErr ? t(emailErr) : undefined);
    setPasswordError(passwordErr ? t(passwordErr) : undefined);

    if (userNameErr || emailErr || passwordErr) return;

    setIsSubmitting(true);
    setSubmitError(undefined);

    try {
      const response = await gameApi.createGame({
        userName: userName.trim(),
        email: email || undefined,
        password: password || undefined,
      });

      setSession({
        handle: response.handle,
        playerId: response.playerId,
        role: "guardian",
        isPasswordProtected: response.passwordProtected,
        hasEmail: response.hasEmail,
      });

      void navigate(`/game/${response.handle}`);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className={styles.page}>
      <h1 className={styles.title}>{t("game.create.title")}</h1>
      <form
        className={styles.form}
        onSubmit={(e) => {
          void handleSubmit(e);
        }}
        noValidate
      >
        <FormField
          id="userName"
          label={t("game.create.userName.label")}
          value={userName}
          onChange={setUserName}
          error={userNameError}
          required
          disabled={isSubmitting}
          placeholder={t("game.create.userName.placeholder")}
        />
        <FormField
          id="email"
          label={t("game.create.email.label")}
          type="email"
          value={email}
          onChange={setEmail}
          error={emailError}
          hint={t("game.create.email.hint")}
          disabled={isSubmitting}
        />
        <FormField
          id="password"
          label={t("game.create.password.label")}
          type="password"
          value={password}
          onChange={setPassword}
          error={passwordError}
          hint={t("game.create.password.hint")}
          disabled={isSubmitting}
        />

        <div className={styles.aiToggle}>
          <input
            type="checkbox"
            id="aiEnabled"
            disabled
            aria-describedby="aiComingSoon"
          />
          <label htmlFor="aiEnabled">{t("game.create.ai.label")}</label>
          <span id="aiComingSoon" className={styles.comingSoon}>
            {t("game.create.ai.comingSoon")}
          </span>
        </div>

        {submitError && (
          <p role="alert" className={styles.submitError}>
            {submitError}
          </p>
        )}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={isSubmitting}
        >
          {isSubmitting
            ? t("game.create.submitting")
            : t("game.create.submit")}
        </button>
      </form>
    </main>
  );
}
