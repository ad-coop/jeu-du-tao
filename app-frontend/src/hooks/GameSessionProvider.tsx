import { createContext, useState } from "react";
import type { ReactNode } from "react";

export type GameSession = {
  handle: string;
  playerId: string;
  role: "guardian" | "player";
  isPasswordProtected: boolean;
  hasEmail: boolean;
};

export type GameSessionContextType = {
  session: GameSession | null;
  setSession: (session: GameSession | null) => void;
};

export const GameSessionContext = createContext<GameSessionContextType | null>(null);

export function GameSessionProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<GameSession | null>(null);

  return (
    <GameSessionContext.Provider value={{ session, setSession }}>
      {children}
    </GameSessionContext.Provider>
  );
}
