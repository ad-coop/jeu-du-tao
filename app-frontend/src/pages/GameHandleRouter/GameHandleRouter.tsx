import { useParams } from "react-router";
import { useGameSession } from "../../hooks/useGameSession";
import { JoinGamePage } from "../JoinGamePage/JoinGamePage";
import { WaitingRoomPage } from "../WaitingRoomPage/WaitingRoomPage";

export function GameHandleRouter() {
  const { handle } = useParams<{ handle: string }>();
  const { session } = useGameSession();

  if (!handle) {
    return <JoinGamePage />;
  }

  if (session && session.handle === handle) {
    return <WaitingRoomPage />;
  }

  return <JoinGamePage initialHandle={handle} />;
}
