export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export type CreateGameRequest = {
  userName: string;
  email?: string;
  password?: string;
};

export type CreateGameResponse = {
  handle: string;
  playerId: string;
  passwordProtected: boolean;
  hasEmail: boolean;
};

export type JoinGameRequest = {
  userName: string;
  password?: string;
};

export type JoinGameResponse = {
  playerId: string;
};

export type GameInfoResponse = {
  handle: string;
  state: string;
  passwordProtected: boolean;
};

export type PlayerInfo = {
  id: string;
  name: string;
  role: string;
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let code = "UNKNOWN_ERROR";
    let message = `HTTP ${response.status}`;
    try {
      const body = (await response.json()) as { error?: string; message?: string };
      if (body.error) code = body.error;
      if (body.message) message = body.message;
    } catch {
      // Body is not valid JSON — use default values
    }
    throw new ApiError(response.status, code, message);
  }
  return response.json() as Promise<T>;
}

export const gameApi = {
  createGame: async (request: CreateGameRequest): Promise<CreateGameResponse> => {
    const response = await fetch("/api/games", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    });
    return handleResponse<CreateGameResponse>(response);
  },

  getGameInfo: async (handle: string): Promise<GameInfoResponse> => {
    const response = await fetch(`/api/games/${encodeURIComponent(handle)}`);
    return handleResponse<GameInfoResponse>(response);
  },

  joinGame: async (handle: string, request: JoinGameRequest): Promise<JoinGameResponse> => {
    const response = await fetch(`/api/games/${encodeURIComponent(handle)}/players`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    });
    return handleResponse<JoinGameResponse>(response);
  },

  getPlayers: async (handle: string): Promise<PlayerInfo[]> => {
    const response = await fetch(`/api/games/${encodeURIComponent(handle)}/players`);
    return handleResponse<PlayerInfo[]>(response);
  },

  kickPlayer: async (
    handle: string,
    playerId: string,
    requestingPlayerId: string,
  ): Promise<void> => {
    const response = await fetch(
      `/api/games/${encodeURIComponent(handle)}/players/${encodeURIComponent(playerId)}`,
      {
        method: "DELETE",
        headers: { "X-Player-Id": requestingPlayerId },
      },
    );
    if (!response.ok) {
      let code = "UNKNOWN_ERROR";
      let message = `HTTP ${response.status}`;
      try {
        const body = (await response.json()) as { error?: string; message?: string };
        if (body.error) code = body.error;
        if (body.message) message = body.message;
      } catch {
        // Body is not valid JSON — use default values
      }
      throw new ApiError(response.status, code, message);
    }
  },
};
