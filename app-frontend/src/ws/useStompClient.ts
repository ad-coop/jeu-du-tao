import { Client } from "@stomp/stompjs";
import { useEffect, useRef, useState } from "react";
import type { PlayerInfo } from "../api/gameApi";

type UseStompClientOptions = {
  handle: string;
  playerId: string;
  onPlayersUpdate: (players: PlayerInfo[]) => void;
  onKicked: () => void;
};

export function useStompClient({
  handle,
  playerId,
  onPlayersUpdate,
  onKicked,
}: UseStompClientOptions): { isConnected: boolean } {
  const [isConnected, setIsConnected] = useState(false);
  // Use refs for callbacks to avoid recreating the client on every render
  const onPlayersUpdateRef = useRef(onPlayersUpdate);
  const onKickedRef = useRef(onKicked);

  useEffect(() => {
    onPlayersUpdateRef.current = onPlayersUpdate;
  }, [onPlayersUpdate]);

  useEffect(() => {
    onKickedRef.current = onKicked;
  }, [onKicked]);

  useEffect(() => {
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const wsUrl = `${protocol}://${window.location.host}/ws`;

    const client = new Client({
      brokerURL: wsUrl,
      connectHeaders: { playerId },
      reconnectDelay: 5000,
      onConnect: () => {
        setIsConnected(true);

        client.subscribe(`/topic/games/${handle}/players`, (message) => {
          const players = JSON.parse(message.body) as PlayerInfo[];
          onPlayersUpdateRef.current(players);
        });

        client.subscribe(`/topic/games/${handle}/kick/${playerId}`, () => {
          onKickedRef.current();
        });
      },
      onDisconnect: () => {
        setIsConnected(false);
      },
    });

    client.activate();

    return () => {
      void client.deactivate();
    };
  }, [handle, playerId]);

  return { isConnected };
}
