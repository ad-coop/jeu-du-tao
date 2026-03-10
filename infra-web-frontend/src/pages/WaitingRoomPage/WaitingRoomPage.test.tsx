import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { WaitingRoomPage } from "./WaitingRoomPage";
import { gameApi } from "../../api/gameApi";
import type { GameSession } from "../../hooks/useGameSession";

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

vi.mock("../../api/gameApi", () => ({
  gameApi: {
    getPlayers: vi.fn(),
    kickPlayer: vi.fn(),
  },
}));

vi.mock("../../ws/useStompClient", () => ({
  useStompClient: () => ({ isConnected: true }),
}));

const mockSession: { value: GameSession | null } = { value: null };

vi.mock("../../hooks/useGameSession", () => ({
  useGameSession: () => ({ session: mockSession.value, setSession: vi.fn() }),
  GameSessionProvider: ({ children }: { children: React.ReactNode }) => children,
}));

function renderPage() {
  return render(
    <MemoryRouter>
      <WaitingRoomPage />
    </MemoryRouter>,
  );
}

describe("WaitingRoomPage", () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    vi.mocked(gameApi.getPlayers).mockResolvedValue([]);
    mockSession.value = {
      handle: "abc123",
      playerId: "p1",
      role: "guardian",
      isPasswordProtected: false,
      hasEmail: true,
    };
  });

  it("render_whenSession_showsGameHandle", async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText("abc123")).toBeInTheDocument();
    });
  });

  it("render_whenNoSession_redirectsToHome", () => {
    mockSession.value = null;

    renderPage();

    expect(mockNavigate).toHaveBeenCalledWith("/");
  });

  it("render_whenGuardianWithNoEmail_showsWarningBanner", async () => {
    mockSession.value = {
      handle: "abc123",
      playerId: "p1",
      role: "guardian",
      isPasswordProtected: false,
      hasEmail: false,
    };

    renderPage();

    await waitFor(() => {
      expect(screen.getByRole("note")).toBeInTheDocument();
    });
  });

  it("render_whenGuardianWithEmail_doesNotShowWarningBanner", async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText("abc123")).toBeInTheDocument();
    });

    expect(screen.queryByRole("note")).not.toBeInTheDocument();
  });

  it("render_showsPlayerList", async () => {
    vi.mocked(gameApi.getPlayers).mockResolvedValue([
      { id: "p1", name: "Alice", role: "guardian" },
      { id: "p2", name: "Bob", role: "player" },
    ]);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText("Alice")).toBeInTheDocument();
      expect(screen.getByText("Bob")).toBeInTheDocument();
    });
  });

  it("render_whenGuardianAbsentAndPlayerIsNotGuardian_showsGuardianLeftBanner", async () => {
    mockSession.value = {
      handle: "abc123",
      playerId: "p2",
      role: "player",
      isPasswordProtected: false,
      hasEmail: false,
    };
    vi.mocked(gameApi.getPlayers).mockResolvedValue([
      { id: "p2", name: "Bob", role: "player" },
    ]);

    renderPage();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
  });

  it("render_whenGuardianPresentAndPlayerIsNotGuardian_doesNotShowGuardianLeftBanner", async () => {
    mockSession.value = {
      handle: "abc123",
      playerId: "p2",
      role: "player",
      isPasswordProtected: false,
      hasEmail: false,
    };
    vi.mocked(gameApi.getPlayers).mockResolvedValue([
      { id: "p1", name: "Alice", role: "guardian" },
      { id: "p2", name: "Bob", role: "player" },
    ]);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText("Alice")).toBeInTheDocument();
    });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("render_whenGuardianIsCurrentPlayer_doesNotShowGuardianLeftBanner", async () => {
    vi.mocked(gameApi.getPlayers).mockResolvedValue([
      { id: "p2", name: "Bob", role: "player" },
    ]);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText("Bob")).toBeInTheDocument();
    });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });
});
