import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { gameApi } from "../../api/gameApi";
import type { PlayerInfo } from "../../api/gameApi";
import { GameHandle } from "../../components/GameHandle";
import { PlayerList } from "../../components/PlayerList";
import { useGameSession } from "../../hooks/useGameSession";
import { useTranslation } from "../../i18n";
import { useStompClient } from "../../ws/useStompClient";
import styles from "./WaitingRoomPage.module.css";

export function WaitingRoomPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { session } = useGameSession();
  const [players, setPlayers] = useState<PlayerInfo[]>([]);

  if (!session) {
    void navigate("/");
    return null;
  }

  const { handle, playerId, role, isPasswordProtected, hasEmail } = session;
  const isGuardian = role === "guardian";

  const handleKick = (targetPlayerId: string) => {
    void gameApi.kickPlayer(handle, targetPlayerId, playerId);
  };

  return (
    <WaitingRoomContent
      handle={handle}
      playerId={playerId}
      isGuardian={isGuardian}
      isPasswordProtected={isPasswordProtected}
      hasEmail={hasEmail}
      players={players}
      setPlayers={setPlayers}
      onKick={handleKick}
      onKicked={() => void navigate("/error/kicked")}
      t={t}
    />
  );
}

type WaitingRoomContentProps = {
  handle: string;
  playerId: string;
  isGuardian: boolean;
  isPasswordProtected: boolean;
  hasEmail: boolean;
  players: PlayerInfo[];
  setPlayers: (players: PlayerInfo[]) => void;
  onKick: (playerId: string) => void;
  onKicked: () => void;
  t: (key: string) => string;
};

function WaitingRoomContent({
  handle,
  playerId,
  isGuardian,
  isPasswordProtected,
  hasEmail,
  players,
  setPlayers,
  onKick,
  onKicked,
  t,
}: WaitingRoomContentProps) {
  const [hasLoadedPlayers, setHasLoadedPlayers] = useState(false);

  const handlePlayersUpdate = useCallback(
    (updatedPlayers: PlayerInfo[]) => {
      setPlayers(updatedPlayers);
      setHasLoadedPlayers(true);
    },
    [setPlayers],
  );

  const { isConnected } = useStompClient({
    handle,
    playerId,
    onPlayersUpdate: handlePlayersUpdate,
    onKicked,
  });

  useEffect(() => {
    void gameApi.getPlayers(handle).then(handlePlayersUpdate);
  }, [handle, handlePlayersUpdate]);

  const isGuardianAbsent =
    hasLoadedPlayers && !isGuardian && !players.some((p) => p.role === "guardian");

  return (
    <main className={styles.page}>
      <div className={styles.layout}>
        <section className={styles.handleSection} aria-label={t("game.waiting.handle.label")}>
          <h1 className={styles.title}>{t("game.waiting.title")}</h1>
          <GameHandle
            handle={handle}
            isPasswordProtected={isPasswordProtected}
            copyLabel={t("game.waiting.handle.copy")}
            copiedLabel={t("game.waiting.handle.copied")}
            passwordProtectedLabel={t("game.waiting.handle.passwordProtected")}
          />
          {!isConnected && (
            <p className={styles.connectionStatus} aria-live="polite">
              {t("game.waiting.connecting")}
            </p>
          )}
          {isGuardian && !hasEmail && (
            <div
              className={styles.warningBanner}
              role="note"
              aria-label={t("game.waiting.noEmail.warning")}
            >
              <p>{t("game.waiting.noEmail.message")}</p>
            </div>
          )}
          {isGuardianAbsent && (
            <div className={styles.warningBanner} role="alert">
              <p>{t("game.waiting.guardianLeft.message")}</p>
            </div>
          )}
        </section>

        <section className={styles.playersSection} aria-label={t("game.waiting.players.label")}>
          <h2 className={styles.playersTitle}>{t("game.waiting.players.title")}</h2>
          <PlayerList
            players={players}
            currentPlayerId={playerId}
            isGuardian={isGuardian}
            onKick={isGuardian ? onKick : undefined}
            kickLabel={(name) => t("game.waiting.kick.label") + " " + name}
          />
        </section>
      </div>
    </main>
  );
}
