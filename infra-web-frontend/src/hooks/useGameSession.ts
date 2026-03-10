import { useContext } from "react";
import { GameSessionContext } from "./GameSessionProvider";
import type { GameSessionContextType } from "./GameSessionProvider";

export type { GameSession } from "./GameSessionProvider";
export { GameSessionProvider } from "./GameSessionProvider";

export function useGameSession(): GameSessionContextType {
  const context = useContext(GameSessionContext);
  if (!context) {
    throw new Error("useGameSession must be used within GameSessionProvider");
  }
  return context;
}
