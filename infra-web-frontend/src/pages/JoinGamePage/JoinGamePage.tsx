import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { gameApi } from "../../api/gameApi";
import type { ApiError, GameInfoResponse } from "../../api/gameApi";
import { FormField } from "../../components/FormField";
import { useGameSession } from "../../hooks/useGameSession";
import { useTranslation } from "../../i18n";
import styles from "./JoinGamePage.module.css";

type JoinGamePageProps = {
  initialHandle?: string;
};

export function JoinGamePage({ initialHandle }: JoinGamePageProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { setSession } = useGameSession();

  const [handle, setHandle] = useState(initialHandle ?? "");
  const [gameInfo, setGameInfo] = useState<GameInfoResponse | null>(null);
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [handleError, setHandleError] = useState<string | undefined>();
  const [userNameError, setUserNameError] = useState<string | undefined>();
  const [passwordError, setPasswordError] = useState<string | undefined>();
  const [submitError, setSubmitError] = useState<string | undefined>();
  const [isLoadingGame, setIsLoadingGame] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Auto-load game info when initialHandle is provided
  useEffect(() => {
    if (initialHandle) {
      void loadGameInfo(initialHandle);
    }
  }, [initialHandle]);

  const loadGameInfo = async (gameHandle: string) => {
    setIsLoadingGame(true);
    setHandleError(undefined);
    setSubmitError(undefined);
    try {
      const info = await gameApi.getGameInfo(gameHandle);
      setGameInfo(info);
    } catch (error) {
      const apiError = error as ApiError;
      setHandleError(apiError.message);
      setGameInfo(null);
    } finally {
      setIsLoadingGame(false);
    }
  };

  const handleLookup = (e: React.FormEvent) => {
    e.preventDefault();
    if (!handle.trim()) {
      setHandleError(t("game.join.error.handleRequired"));
      return;
    }
    void navigate(`/game/${handle.trim()}`);
  };

  const handleJoin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!gameInfo) return;

    const trimmedUserName = userName.trim();
    if (!trimmedUserName) {
      setUserNameError(t("game.join.error.userNameRequired"));
      return;
    }
    if (trimmedUserName.length > 50) {
      setUserNameError(t("game.join.error.userNameTooLong"));
      return;
    }
    setUserNameError(undefined);

    if (gameInfo.passwordProtected && !password) {
      setPasswordError(t("game.join.error.passwordRequired"));
      return;
    }
    setPasswordError(undefined);

    setIsSubmitting(true);
    setSubmitError(undefined);

    try {
      const response = await gameApi.joinGame(handle.trim(), {
        userName: trimmedUserName,
        password: password || undefined,
      });

      setSession({
        handle: handle.trim(),
        playerId: response.playerId,
        role: "player",
        isPasswordProtected: gameInfo.passwordProtected,
        hasEmail: false,
      });

      void navigate(`/game/${handle.trim()}`);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const showHandleField = !initialHandle;
  const showJoinForm = gameInfo !== null && !isLoadingGame;

  return (
    <main className={styles.page}>
      <h1 className={styles.title}>{t("game.join.title")}</h1>

      {showHandleField && (
        <form
          className={styles.form}
          onSubmit={handleLookup}
          noValidate
        >
          <FormField
            id="handle"
            label={t("game.join.handle.label")}
            value={handle}
            onChange={setHandle}
            error={handleError}
            required
            disabled={isLoadingGame}
            placeholder={t("game.join.handle.placeholder")}
          />
          <button
            type="submit"
            className={styles.submitButton}
            disabled={isLoadingGame}
          >
            {t("game.join.lookup")}
          </button>
        </form>
      )}

      {!showHandleField && handleError && (
        <p role="alert" className={styles.errorBanner}>
          {handleError}
        </p>
      )}

      {isLoadingGame && (
        <p aria-live="polite">{t("game.join.loading")}</p>
      )}

      {showJoinForm && (
        <form
          className={styles.form}
          onSubmit={(e) => {
            void handleJoin(e);
          }}
          noValidate
        >
          <FormField
            id="userName"
            label={t("game.join.userName.label")}
            value={userName}
            onChange={setUserName}
            error={userNameError}
            required
            disabled={isSubmitting}
            placeholder={t("game.join.userName.placeholder")}
          />

          {gameInfo.passwordProtected && (
            <FormField
              id="joinPassword"
              label={t("game.join.password.label")}
              type="password"
              value={password}
              onChange={setPassword}
              error={passwordError}
              required
              disabled={isSubmitting}
            />
          )}

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
              ? t("game.join.submitting")
              : t("game.join.submit")}
          </button>
        </form>
      )}
    </main>
  );
}
