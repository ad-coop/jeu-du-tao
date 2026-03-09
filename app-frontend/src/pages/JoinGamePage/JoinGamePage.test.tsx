import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { JoinGamePage } from "./JoinGamePage";
import { gameApi } from "../../api/gameApi";

const mockNavigate = vi.fn();
const mockSetSession = vi.fn();

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
    getGameInfo: vi.fn(),
    joinGame: vi.fn(),
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

vi.mock("../../hooks/useGameSession", () => ({
  useGameSession: () => ({ session: null, setSession: mockSetSession }),
  GameSessionProvider: ({ children }: { children: React.ReactNode }) => children,
}));

function renderPage(initialHandle?: string) {
  return render(
    <MemoryRouter>
      <JoinGamePage initialHandle={initialHandle} />
    </MemoryRouter>,
  );
}

describe("JoinGamePage", () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    mockSetSession.mockClear();
    vi.mocked(gameApi.getGameInfo).mockClear();
    vi.mocked(gameApi.joinGame).mockClear();
  });

  it("render_showsHandleFieldWhenNoInitialHandle", () => {
    renderPage();

    expect(screen.getByLabelText(/game.join.handle.label/)).toBeInTheDocument();
    expect(screen.queryByLabelText(/game.join.userName.label/)).not.toBeInTheDocument();
  });

  it("render_whenInitialHandleProvided_loadsGameInfoAndShowsJoinForm", async () => {
    vi.mocked(gameApi.getGameInfo).mockResolvedValue({
      handle: "abc123",
      state: "WAITING",
      passwordProtected: false,
    });

    renderPage("abc123");

    await waitFor(() => {
      expect(screen.getByLabelText(/game.join.userName.label/)).toBeInTheDocument();
    });
    expect(screen.queryByLabelText(/game.join.handle.label/)).not.toBeInTheDocument();
  });

  it("handleLookup_whenHandleEmpty_showsError", async () => {
    renderPage();

    fireEvent.click(screen.getByRole("button", { name: /game.join.lookup/ }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
    expect(gameApi.getGameInfo).not.toHaveBeenCalled();
  });

  it("handleLookup_whenHandleValid_navigatesToGameRoute", () => {
    renderPage();

    fireEvent.change(screen.getByLabelText(/game.join.handle.label/), {
      target: { value: "abc123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /game.join.lookup/ }));

    expect(mockNavigate).toHaveBeenCalledWith("/game/abc123");
    expect(gameApi.getGameInfo).not.toHaveBeenCalled();
  });

  it("loadGameInfo_whenInitialHandleAndApiError_showsErrorBanner", async () => {
    vi.mocked(gameApi.getGameInfo).mockRejectedValue(
      new Error("Game not found"),
    );

    renderPage("unknown");

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
  });

  it("join_whenPasswordProtected_showsPasswordField", async () => {
    vi.mocked(gameApi.getGameInfo).mockResolvedValue({
      handle: "abc123",
      state: "WAITING",
      passwordProtected: true,
    });

    renderPage("abc123");

    await waitFor(() => {
      expect(screen.getByLabelText(/game.join.password.label/)).toBeInTheDocument();
    });
  });

  it("join_whenValid_callsApiAndNavigates", async () => {
    vi.mocked(gameApi.getGameInfo).mockResolvedValue({
      handle: "abc123",
      state: "WAITING",
      passwordProtected: false,
    });
    vi.mocked(gameApi.joinGame).mockResolvedValue({ playerId: "player-2" });

    renderPage("abc123");

    await waitFor(() => {
      expect(screen.getByLabelText(/game.join.userName.label/)).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/game.join.userName.label/), {
      target: { value: "Bob" },
    });
    fireEvent.click(screen.getByRole("button", { name: /game.join.submit/ }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/game/abc123");
    });

    expect(gameApi.joinGame).toHaveBeenCalledWith("abc123", {
      userName: "Bob",
      password: undefined,
    });
  });
});
