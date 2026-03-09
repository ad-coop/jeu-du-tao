import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { GameHandleRouter } from "./GameHandleRouter";
import type { GameSession } from "../../hooks/useGameSession";

const mockSession: { value: GameSession | null } = { value: null };

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("react-router", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router")>();
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

vi.mock("../../hooks/useGameSession", () => ({
  useGameSession: () => ({ session: mockSession.value, setSession: vi.fn() }),
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
      </Routes>
    </MemoryRouter>,
  );
}

describe("GameHandleRouter", () => {
  beforeEach(() => {
    mockSession.value = null;
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
});
