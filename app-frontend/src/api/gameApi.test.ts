import { ApiError, gameApi } from "./gameApi";

describe("gameApi", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe("createGame", () => {
    it("createGame_whenSuccess_returnsResponse", async () => {
      const responseData = {
        handle: "abc123",
        playerId: "player-1",
        passwordProtected: false,
        hasEmail: true,
      };
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(responseData),
      } as Response);

      const result = await gameApi.createGame({ userName: "Alice", email: "alice@example.com" });

      expect(result).toEqual(responseData);
      expect(fetch).toHaveBeenCalledWith("/api/games", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName: "Alice", email: "alice@example.com" }),
      });
    });

    it("createGame_whenError_throwsApiError", async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        status: 400,
        json: () => Promise.resolve({ error: "INVALID_INPUT", message: "Invalid user name" }),
      } as Response);

      await expect(gameApi.createGame({ userName: "" })).rejects.toThrow(ApiError);
      await expect(gameApi.createGame({ userName: "" })).rejects.toMatchObject({
        status: 400,
        code: "INVALID_INPUT",
        message: "Invalid user name",
      });
    });

    it("createGame_whenErrorBodyIsNotJson_throwsApiErrorWithDefaultMessage", async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        status: 500,
        json: () => Promise.reject(new Error("Not JSON")),
      } as Response);

      await expect(gameApi.createGame({ userName: "Alice" })).rejects.toMatchObject({
        status: 500,
        code: "UNKNOWN_ERROR",
      });
    });
  });

  describe("getGameInfo", () => {
    it("getGameInfo_whenSuccess_returnsResponse", async () => {
      const responseData = { handle: "abc123", state: "WAITING", passwordProtected: false };
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(responseData),
      } as Response);

      const result = await gameApi.getGameInfo("abc123");

      expect(result).toEqual(responseData);
      expect(fetch).toHaveBeenCalledWith("/api/games/abc123");
    });

    it("getGameInfo_whenNotFound_throwsApiError", async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        status: 404,
        json: () => Promise.resolve({ error: "GAME_NOT_FOUND", message: "Game not found" }),
      } as Response);

      await expect(gameApi.getGameInfo("unknown")).rejects.toMatchObject({
        status: 404,
        code: "GAME_NOT_FOUND",
      });
    });
  });

  describe("joinGame", () => {
    it("joinGame_whenSuccess_returnsResponse", async () => {
      const responseData = { playerId: "player-2" };
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(responseData),
      } as Response);

      const result = await gameApi.joinGame("abc123", { userName: "Bob" });

      expect(result).toEqual(responseData);
      expect(fetch).toHaveBeenCalledWith("/api/games/abc123/players", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName: "Bob" }),
      });
    });

    it("joinGame_whenError_throwsApiError", async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        status: 403,
        json: () => Promise.resolve({ error: "WRONG_PASSWORD", message: "Wrong password" }),
      } as Response);

      await expect(gameApi.joinGame("abc123", { userName: "Bob" })).rejects.toMatchObject({
        status: 403,
        code: "WRONG_PASSWORD",
      });
    });
  });

  describe("getPlayers", () => {
    it("getPlayers_whenSuccess_returnsPlayerList", async () => {
      const responseData = [
        { id: "p1", name: "Alice", role: "guardian" },
        { id: "p2", name: "Bob", role: "player" },
      ];
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(responseData),
      } as Response);

      const result = await gameApi.getPlayers("abc123");

      expect(result).toEqual(responseData);
      expect(fetch).toHaveBeenCalledWith("/api/games/abc123/players");
    });
  });

  describe("kickPlayer", () => {
    it("kickPlayer_whenSuccess_resolves", async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({}),
      } as Response);

      await expect(gameApi.kickPlayer("abc123", "p2", "p1")).resolves.toBeUndefined();
      expect(fetch).toHaveBeenCalledWith("/api/games/abc123/players/p2", {
        method: "DELETE",
        headers: { "X-Player-Id": "p1" },
      });
    });

    it("kickPlayer_whenError_throwsApiError", async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        status: 403,
        json: () => Promise.resolve({ error: "NOT_GUARDIAN", message: "Not a guardian" }),
      } as Response);

      await expect(gameApi.kickPlayer("abc123", "p2", "p1")).rejects.toMatchObject({
        status: 403,
        code: "NOT_GUARDIAN",
      });
    });
  });
});
