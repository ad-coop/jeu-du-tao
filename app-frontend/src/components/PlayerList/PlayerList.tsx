import styles from "./PlayerList.module.css";

type Player = {
  id: string;
  name: string;
  role: string;
};

type PlayerListProps = {
  players: Player[];
  currentPlayerId: string;
  isGuardian: boolean;
  onKick?: (playerId: string) => void;
  kickLabel: (name: string) => string;
};

export function PlayerList({
  players,
  currentPlayerId,
  isGuardian,
  onKick,
  kickLabel,
}: PlayerListProps) {
  const sortedPlayers = [...players].sort((a, b) => {
    if (a.role === "guardian") return -1;
    if (b.role === "guardian") return 1;
    return 0;
  });

  return (
    <ul className={styles.list} aria-live="polite">
      {sortedPlayers.map((player) => (
        <li key={player.id} className={styles.item}>
          <span className={styles.name}>{player.name}</span>
          {player.role === "guardian" && (
            <span className={styles.badge}>Gardien</span>
          )}
          {isGuardian && player.id !== currentPlayerId && onKick && (
            <button
              type="button"
              className={styles.kickButton}
              onClick={() => onKick(player.id)}
              aria-label={kickLabel(player.name)}
            >
              &times;
            </button>
          )}
        </li>
      ))}
    </ul>
  );
}
