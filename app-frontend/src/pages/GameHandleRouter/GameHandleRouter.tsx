import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router";
import { gameApi } from "../../api/gameApi";
import { useGameSession } from "../../hooks/useGameSession";
import { JoinGamePage } from "../JoinGamePage/JoinGamePage";
import { WaitingRoomPage } from "../WaitingRoomPage/WaitingRoomPage";

export function GameHandleRouter() {
  const { handle } = useParams<{ handle: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  const { session, setSession } = useGameSession();
  const navigate = useNavigate();
  const [isRestoring, setIsRestoring] = useState(false);

  const token = searchParams.get("token");

  useEffect(() => {
    if (!handle || !token || (session && session.handle === handle)) {
      return;
    }

    setIsRestoring(true);
    gameApi
      .restoreGame(handle, token)
      .then((result) => {
        setSession({
          handle,
          playerId: result.playerId,
          role: "guardian",
          isPasswordProtected: result.passwordProtected,
          hasEmail: result.hasEmail,
        });
        setSearchParams({}, { replace: true });
      })
      .catch(() => {
        navigate("/error/invalid-magic-link", { replace: true });
      })
      .finally(() => {
        setIsRestoring(false);
      });
  }, [handle, token]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!handle) {
    return <JoinGamePage />;
  }

  if (isRestoring) {
    return null;
  }

  if (session && session.handle === handle) {
    return <WaitingRoomPage />;
  }

  return <JoinGamePage initialHandle={handle} />;
}
