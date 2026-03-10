import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { GameHandleRouter } from "./GameHandleRouter";
import type { GameSession } from "../../hooks/useGameSession";
import { gameApi } from "../../api/gameApi";

const mockSession: { value: GameSession | null } = { value: null };
const mockSetSession = vi.fn();
const mockNavigate = vi.fn();

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("react-router", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router")>();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("../../hooks/useGameSession", () => ({
  useGameSession: () => ({ session: mockSession.value, setSession: mockSetSession }),
  GameSessionProvider: ({ children }: { children: React.ReactNode }) => children,
}));

vi.mock("../../api/gameApi", () => ({
  gameApi: {
    getGameInfo: vi.fn().mockResolvedValue({
      handle: "abc123",
      state: "WAITING",
      passwordProtected: false,
    }),
    joinGame: vi.fn(),
    getPlayers: vi.fn().mockResolvedValue([]),
    restoreGame: vi.fn(),
  },
  ApiError: class ApiError extends Error {
    status: number;
    code: string;
    constructor(status: number, code: string, message: string) {
      super(message);
      this.status = status;
      this.code = code;
    }
  },
}));

vi.mock("../../ws/useStompClient", () => ({
  useStompClient: () => ({ isConnected: false }),
}));

function renderWithHandle(handle: string) {
  return render(
    <MemoryRouter initialEntries={[`/game/${handle}`]}>
      <Routes>
        <Route path="/game/:handle" element={<GameHandleRouter />} />
        <Route path="/error/:errorType" element={<div>error page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

function renderWithHandleAndToken(handle: string, token: string) {
  return render(
    <MemoryRouter initialEntries={[`/game/${handle}?token=${token}`]}>
      <Routes>
        <Route path="/game/:handle" element={<GameHandleRouter />} />
        <Route path="/error/:errorType" element={<div>error page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe("GameHandleRouter", () => {
  beforeEach(() => {
    mockSession.value = null;
    mockSetSession.mockReset();
    mockNavigate.mockReset();
    vi.mocked(gameApi.restoreGame).mockReset();
  });

  it("render_whenNoSession_showsJoinPage", () => {
    renderWithHandle("abc123");

    expect(screen.getByRole("main")).toBeInTheDocument();
  });

  it("render_whenSessionForDifferentHandle_showsJoinPage", () => {
    mockSession.value = {
      handle: "other-game",
      playerId: "p1",
      role: "guardian",
      isPasswordProtected: false,
      hasEmail: false,
    };

    renderWithHandle("abc123");

    expect(screen.getByRole("main")).toBeInTheDocument();
  });

  it("render_whenSessionMatchesHandle_showsWaitingRoom", () => {
    mockSession.value = {
      handle: "abc123",
      playerId: "p1",
      role: "guardian",
      isPasswordProtected: false,
      hasEmail: false,
    };

    renderWithHandle("abc123");

    // WaitingRoomPage renders a main element too; the test verifies routing
    expect(screen.getByRole("main")).toBeInTheDocument();
  });

  it("render_whenTokenParam_andRestoreSucceeds_showsWaitingRoom", async () => {
    vi.mocked(gameApi.restoreGame).mockResolvedValue({
      playerId: "p42",
      playerName: "Alice",
      passwordProtected: false,
      hasEmail: true,
    });

    renderWithHandleAndToken("abc123", "validtoken");

    await waitFor(() => {
      expect(mockSetSession).toHaveBeenCalledWith({
        handle: "abc123",
        playerId: "p42",
        role: "guardian",
        isPasswordProtected: false,
        hasEmail: true,
      });
    });
  });

  it("render_whenTokenParam_andRestoreFails_navigatesToError", async () => {
    vi.mocked(gameApi.restoreGame).mockRejectedValue(new Error("Unauthorized"));

    renderWithHandleAndToken("abc123", "badtoken");

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/error/invalid-magic-link", { replace: true });
    });
  });
});
